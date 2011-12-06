package org.linuxstuff.mojo.licensing;

import java.util.Collection;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * 
 * @author idcmp
 * 
 * @goal check
 * @phase pre-integration-test
 * @requiresDependencyResolution test
 * @requiresProject true
 * @since 1.0
 */
public class CheckMojo extends AbstractLicensingMojo {

	/**
	 * Fail the build if any dependencies are either under disliked licenses or
	 * are missing licensing information.
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {

		int dislikedArtifacts = 0, missingLicense = 0;
		readLicensingRequirements();

		Collection<MavenProject> projects = getProjectDependencies();
		for (MavenProject mavenProject : projects) {
			if (!hasLicense(mavenProject)) {
				getLog().warn("Licensing: The artifact " + mavenProject.getId() + " has no license specified.");
				missingLicense++;
			} else if (isDisliked(mavenProject)) {
				getLog().warn("Licensing: The artifact " + mavenProject.getId() + " is under a disliked license.");
				dislikedArtifacts++;
			}
		}

		if (dislikedArtifacts > 0 && missingLicense > 0) {
			throw new MojoFailureException("This project has " + dislikedArtifacts + " disliked artifact"
					+ ((dislikedArtifacts == 1) ? "" : "s") + " and " + missingLicense + " artifact"
					+ ((missingLicense == 1) ? "" : "s") + " missing licensing information.");
		} else if (missingLicense > 0) {
			throw new MojoFailureException("This project has " + missingLicense + " artifact"
					+ ((missingLicense == 1) ? "" : "s") + " missing licensing information.");
		} else if (dislikedArtifacts > 0) {
			throw new MojoFailureException("This project has " + dislikedArtifacts + " disliked artifact"
					+ ((dislikedArtifacts == 1) ? "" : "s") + ".");
		}
	}

}
