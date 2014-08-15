package org.linuxstuff.mojo.licensing;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.linuxstuff.mojo.licensing.model.ArtifactWithLicenses;
import org.linuxstuff.mojo.licensing.model.LicensingReport;

/**
 * Determine licensing information of all dependencies. This is generally
 * obtained by dependencies providing a license block in their POM. However this
 * plugin supports a requirements file which can supplement licensing
 * information for artifacts missing licensing information.
 * 
 * @goal check
 * @phase verify
 * @threadSafe
 * @requiresDependencyResolution test
 * @requiresProject true
 * @since 1.0
 */
public class CheckMojo extends AbstractLicensingMojo {

    /**
     * Maven ProjectHelper.
     *
     * @component
     * @readonly
     */
    protected MavenProjectHelper projectHelper;

    /**
     * A fail the build if any artifacts are missing licensing information.
     * 
     * @parameter property="failIfMissing" default-value="true"
     * @since 1.0
     */
    protected boolean failIfMissing;

    /**
     * A fail the build if any artifacts have disliked licenses.
     * 
     * @parameter property="failIfDisliked" default-value="true"
     * @since 1.0
     */
    protected boolean failIfDisliked;

    /**
     * Should the generated report be attached to the reactor.
     *
     * @parameter property="attach" default-value="true"
     */
    protected boolean attach;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        if (skip) {
            getLog().debug("licensing.skip=true, not doing anything.");
            return;
        }

        readLicensingRequirements();

        LicensingReport report = generateReport(project);

        File file = new File(project.getBuild().getDirectory(), thirdPartyLicensingFilename);

        report.writeReport(file);

        if (attach) {
            projectHelper.attachArtifact(project, "xml", "license-report", file);
        }

        checkForFailure(report);

    }

    protected LicensingReport generateReport(MavenProject project) {

        LicensingReport aReport = new LicensingReport();

        Collection<MavenProject> projects = getProjectDependencies(project);
        for (MavenProject mavenProject : projects) {

            ArtifactWithLicenses entry = new ArtifactWithLicenses();

            entry.setArtifactId(mavenProject.getId());
            entry.setName(mavenProject.getName());
            entry.setProjectUrl(mavenProject.getUrl());

            Set<String> licenses = collectLicensesForMavenProject(mavenProject);

            if (licenses.isEmpty()) {
                getLog().warn("Licensing: The artifact " + mavenProject.getId() + " has no license specified.");
                aReport.addMissingLicense(entry);
            } else {
                for (String license : licenses) {
                    entry.addLicense(license);
                }

                if (isDisliked(mavenProject)) {
                    getLog().warn(
                            "Licensing: The artifact " + mavenProject.getId() + " is only under a disliked license.");
                    aReport.addDislikedArtifact(entry);
                } else {
                    aReport.addLicensedArtifact(entry);
                }

            }

        }

        return aReport;
    }

    protected void checkForFailure(LicensingReport report) throws MojoFailureException {
        long disliked = report.getDislikedArtifacts().size();
        long missing = report.getLicenseMissing().size();

        if (disliked > 0 && missing > 0 && failIfDisliked && failIfMissing) {
            throw new MojoFailureException("This project has " + disliked + " disliked artifact"
                    + ((disliked == 1) ? "" : "s") + " and " + missing + " artifact" + ((missing == 1) ? "" : "s")
                    + " missing licensing information.");
        } else if (missing > 0 && failIfMissing) {
            throw new MojoFailureException("This project has " + missing + " artifact" + ((missing == 1) ? "" : "s")
                    + " missing licensing information.");
        } else if (disliked > 0 && failIfDisliked) {
            throw new MojoFailureException("This project has " + disliked + " disliked artifact"
                    + ((disliked == 1) ? "" : "s") + ".");
        }

    }
}
