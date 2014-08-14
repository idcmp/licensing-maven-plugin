/*
 * #%L
 * License Maven Plugin
 *
 * $Id: DefaultDependenciesTool.java 14411 2011-08-10 22:09:34Z tchemit $
 * $HeadURL: http://svn.codehaus.org/mojo/tags/license-maven-plugin-1.0/src/main/java/org/codehaus/mojo/license/DefaultDependenciesTool.java $
 * %%
 * Copyright (C) 2011 CodeLutin, Codehaus, Tony Chemit
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
package org.linuxstuff.mojo.licensing;

import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.logging.Logger;

/**
 * Default implementation of the {@link DependenciesTool}.
 * 
 * @author tchemit <chemit@codelutin.com> (originally)
 * @plexus.component role="org.linuxstuff.mojo.licensing.DependenciesTool"
 *                   role-hint="default"
 *                   
 * @threadSafe
 * @since 1.0
 */
public class DefaultDependenciesTool extends AbstractLogEnabled implements DependenciesTool {

    public static final String INVALID_PATTERN_MESSAGE = "The pattern specified by expression <%s> seems to be invalid.";

    /**
     * Project builder.
     * 
     * @plexus.requirement
     */
    private MavenProjectBuilder mavenProjectBuilder;

    @Override
    public SortedMap<String, MavenProject> loadProjectDependencies(MavenProject project,
            MavenProjectDependenciesConfigurator configuration, ArtifactRepository localRepository,
            List<ArtifactRepository> remoteRepositories, SortedMap<String, MavenProject> cache) {

        boolean haveNoIncludedGroups = StringUtils.isEmpty(configuration.getIncludedGroups());
        boolean haveNoIncludedArtifacts = StringUtils.isEmpty(configuration.getIncludedArtifacts());

        boolean haveExcludedGroups = StringUtils.isNotEmpty(configuration.getExcludedGroups());
        boolean haveExcludedArtifacts = StringUtils.isNotEmpty(configuration.getExcludedArtifacts());
        boolean haveExclusions = haveExcludedGroups || haveExcludedArtifacts;

        Pattern includedGroupPattern = null;
        Pattern includedArtifactPattern = null;
        Pattern excludedGroupPattern = null;
        Pattern excludedArtifactPattern = null;

        if (!haveNoIncludedGroups) {
            includedGroupPattern = Pattern.compile(configuration.getIncludedGroups());
        }
        if (!haveNoIncludedArtifacts) {
            includedArtifactPattern = Pattern.compile(configuration.getIncludedArtifacts());
        }
        if (haveExcludedGroups) {
            excludedGroupPattern = Pattern.compile(configuration.getExcludedGroups());
        }
        if (haveExcludedArtifacts) {
            excludedArtifactPattern = Pattern.compile(configuration.getExcludedArtifacts());
        }

        Set<?> depArtifacts;

        if (configuration.isIncludeTransitiveDependencies()) {
            // All project dependencies
            depArtifacts = project.getArtifacts();
        } else {
            // Only direct project dependencies
            depArtifacts = project.getDependencyArtifacts();
        }

        List<String> includedScopes = configuration.getIncludedScopes();
        List<String> excludeScopes = configuration.getExcludedScopes();

        SortedMap<String, MavenProject> result = new TreeMap<String, MavenProject>();

        for (Object o : depArtifacts) {
            Artifact artifact = (Artifact) o;

            String scope = artifact.getScope();
            if (!includedScopes.isEmpty() && !includedScopes.contains(scope)) {

                // not in included scopes
                continue;
            }
            {
                if (excludeScopes.contains(scope)) {

                    // in excluded scopes
                    continue;
                }
            }

            Logger log = getLogger();

            String id = artifact.getId();

            log.debug("detected artifact " + id);

            // Check if the project should be included
            // If there is no specified artifacts and group to include, include
            // all
            boolean isToInclude = haveNoIncludedArtifacts && haveNoIncludedGroups
                    || isIncludable(artifact, includedGroupPattern, includedArtifactPattern);

            // Check if the project should be excluded
            boolean isToExclude = isToInclude && haveExclusions
                    && isExcludable(artifact, excludedGroupPattern, excludedArtifactPattern);

            if (!isToInclude || isToExclude) {
                log.debug("skip artifact " + id);
                continue;
            }

            MavenProject depMavenProject = null;

            if (cache != null) {

                // try to get project from cache
                depMavenProject = cache.get(id);
            }

            if (depMavenProject != null) {
                log.debug("add dependency [" + id + "] (from cache)");
            } else {

                // build project

                try {
                    depMavenProject = mavenProjectBuilder.buildFromRepository(artifact, remoteRepositories,
                            localRepository, true);
                } catch (ProjectBuildingException e) {
                    log.warn("Unable to obtain POM for artifact : " + artifact, e);
                    continue;
                }

                log.debug("add dependency [" + id + "]");
                if (cache != null) {

                    // store it also in cache
                    cache.put(id, depMavenProject);
                }
            }

            // keep the project
            result.put(id, depMavenProject);
        }

        return result;
    }

    protected boolean isIncludable(Artifact project, Pattern includedGroupPattern, Pattern includedArtifactPattern) {

        Logger log = getLogger();

        // check if the groupId of the project should be included
        if (includedGroupPattern != null) {
            // we have some defined license filters
            try {
                Matcher matchGroupId = includedGroupPattern.matcher(project.getGroupId());
                if (matchGroupId.find()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Include " + project.getGroupId());
                    }
                    return true;
                }
            } catch (PatternSyntaxException e) {
                log.warn(String.format(INVALID_PATTERN_MESSAGE, includedGroupPattern.pattern()));
            }
        }

        // check if the artifactId of the project should be included
        if (includedArtifactPattern != null) {
            // we have some defined license filters
            try {
                Matcher matchGroupId = includedArtifactPattern.matcher(project.getArtifactId());
                if (matchGroupId.find()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Include " + project.getArtifactId());
                    }
                    return true;
                }
            } catch (PatternSyntaxException e) {
                log.warn(String.format(INVALID_PATTERN_MESSAGE, includedArtifactPattern.pattern()));
            }
        }

        return false;
    }

    protected boolean isExcludable(Artifact project, Pattern excludedGroupPattern, Pattern excludedArtifactPattern) {

        Logger log = getLogger();

        // check if the groupId of the project should be included
        if (excludedGroupPattern != null) {
            // we have some defined license filters
            try {
                Matcher matchGroupId = excludedGroupPattern.matcher(project.getGroupId());
                if (matchGroupId.find()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Exclude " + project.getGroupId());
                    }
                    return true;
                }
            } catch (PatternSyntaxException e) {
                log.warn(String.format(INVALID_PATTERN_MESSAGE, excludedGroupPattern.pattern()));
            }
        }

        // check if the artifactId of the project should be included
        if (excludedArtifactPattern != null) {
            // we have some defined license filters
            try {
                Matcher matchGroupId = excludedArtifactPattern.matcher(project.getArtifactId());
                if (matchGroupId.find()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Exclude " + project.getArtifactId());
                    }
                    return true;
                }
            } catch (PatternSyntaxException e) {
                log.warn(String.format(INVALID_PATTERN_MESSAGE, excludedArtifactPattern.pattern()));
            }
        }

        return false;
    }
}
