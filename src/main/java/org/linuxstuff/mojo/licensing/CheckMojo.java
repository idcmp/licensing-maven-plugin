package org.linuxstuff.mojo.licensing;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
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
 * @requiresDependencyResolution test
 * @requiresProject true
 * @since 1.0
 */
public class CheckMojo extends AbstractLicensingMojo {

	/**
	 * A fail the build if any artifacts are missing licensing information.
	 * 
	 * @parameter expression="${failIfMissing}" default-value="true"
	 * @since 1.0
	 */
	protected boolean failIfMissing;

	/**
	 * A fail the build if any artifacts have disliked licenses.
	 * 
	 * @parameter expression="${failIfDisliked}" default-value="true"
	 * @since 1.0
	 */
	protected boolean failIfDisliked;

	private LicensingReport report;

	/**
	 * Fail the build if any dependencies are either under disliked licenses or
	 * are missing licensing information.
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {

		if (skip) {
			getLog().debug("licensing.skip=true, not doing anything.");
			return;
		}

		readLicensingRequirements();

		report = new LicensingReport();

		Collection<MavenProject> projects = getProjectDependencies();
		for (MavenProject mavenProject : projects) {

			ArtifactWithLicenses entry = new ArtifactWithLicenses();

			entry.setArtifactId(mavenProject.getId());
			entry.setName(mavenProject.getName());

			Set<String> licenses = collectLicensesForMavenProject(mavenProject);

			if (licenses.isEmpty()) {
				getLog().warn("Licensing: The artifact " + mavenProject.getId() + " has no license specified.");
				report.addMissingLicense(entry);
			} else {
				for (String license : licenses) {
					entry.addLicense(license);
				}

				if (isDisliked(mavenProject)) {
					getLog().warn("Licensing: The artifact " + mavenProject.getId() + " is only under a disliked license.");
					report.addDislikedArtifact(entry);
				} else {
					report.addLicensedArtifact(entry);
				}

			}

		}

		File file = new File(project.getBuild().getDirectory(), thirdPartyLicensingFilename);

		report.writeReport(file);

		checkForFailure();

	}

	protected void checkForFailure() throws MojoFailureException {
		long disliked = report.getDislikedArtifacts().size();
		long missing = report.getLicenseMissing().size();

		if (disliked > 0 && missing > 0 && failIfDisliked && failIfMissing) {
			throw new MojoFailureException("This project has " + disliked + " disliked artifact" + ((disliked == 1) ? "" : "s") + " and " + missing + " artifact" + ((missing == 1) ? "" : "s")
					+ " missing licensing information.");
		} else if (missing > 0 && failIfMissing) {
			throw new MojoFailureException("This project has " + missing + " artifact" + ((missing == 1) ? "" : "s") + " missing licensing information.");
		} else if (disliked > 0 && failIfDisliked) {
			throw new MojoFailureException("This project has " + disliked + " disliked artifact" + ((disliked == 1) ? "" : "s") + ".");
		}

	}
}
