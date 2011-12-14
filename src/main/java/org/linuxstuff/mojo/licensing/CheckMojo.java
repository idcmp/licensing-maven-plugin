package org.linuxstuff.mojo.licensing;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @goal check
 * @phase pre-integration-test
 * @requiresDependencyResolution test
 * @requiresProject true
 * @since 1.0
 */
public class CheckMojo extends AbstractLicensingMojo {

	private static final String LICENSING_CHECK = "licensing-check";

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

		int dislikedArtifacts = 0, missingLicense = 0;
		readLicensingRequirements();

		report = new LicensingReport();
		try {
			report.initialize();
		} catch (ParserConfigurationException e) {
			throw new MojoExecutionException("Failed to initialize LicensingReport", e);
		}

		Document doc = report.getDocument();
		Element root = doc.createElement("licensing");
		Element artifacts = doc.createElement("artifacts");
		Element missing = doc.createElement("license-missing");
		Element disliked = doc.createElement("license-disliked");

		doc.appendChild(root);

		root.appendChild(artifacts);
		root.appendChild(missing);
		root.appendChild(disliked);

		Collection<MavenProject> projects = getProjectDependencies();
		for (MavenProject mavenProject : projects) {

			Element artifact = doc.createElement("artifact");
			artifact.setAttribute("id", mavenProject.getId());

			Set<String> licenses = collectLicensesForMavenProject(mavenProject);

			if (licenses.isEmpty()) {
				getLog().warn("Licensing: The artifact " + mavenProject.getId() + " has no license specified.");
				missingLicense++;
				missing.appendChild(artifact.cloneNode(true));
			} else {
				for (String license : licenses) {
					Element licenseXml = doc.createElement("license");
					licenseXml.setTextContent(license);
					artifact.appendChild(licenseXml);
				}
			}

			if (!licenses.isEmpty() && isDisliked(mavenProject)) {
				getLog().warn("Licensing: The artifact " + mavenProject.getId() + " is only under a disliked license.");
				dislikedArtifacts++;
				disliked.appendChild(artifact.cloneNode(true));
			}

			artifacts.appendChild(artifact);

		}

		root.setAttribute("missing-licenses", "" + missingLicense);
		root.setAttribute("disliked-licenses", "" + dislikedArtifacts);
		root.setAttribute(LICENSING_CHECK, (missingLicense + dislikedArtifacts > 0) ? "fail" : "pass");
		writeReport();

		if (dislikedArtifacts > 0 && missingLicense > 0 && failIfDisliked && failIfMissing) {
			throw new MojoFailureException("This project has " + dislikedArtifacts + " disliked artifact"
					+ ((dislikedArtifacts == 1) ? "" : "s") + " and " + missingLicense + " artifact"
					+ ((missingLicense == 1) ? "" : "s") + " missing licensing information.");
		} else if (missingLicense > 0 && failIfMissing) {
			throw new MojoFailureException("This project has " + missingLicense + " artifact"
					+ ((missingLicense == 1) ? "" : "s") + " missing licensing information.");
		} else if (dislikedArtifacts > 0 && failIfDisliked) {
			throw new MojoFailureException("This project has " + dislikedArtifacts + " disliked artifact"
					+ ((dislikedArtifacts == 1) ? "" : "s") + ".");
		}

	}

	private void writeReport() throws MojoExecutionException {

		File file = new File(project.getBuild().getDirectory(), "third-party-licensing.xml");

		try {
			getLog().debug("Writing license report to " + file);
			FileUtil.createNewFile(file);
			report.write(file.toString());
		} catch (TransformerException e) {
			throw new MojoExecutionException("Failure while writing " + file, e);
		} catch (IOException e) {
			throw new MojoExecutionException("Failure while creating new file " + file, e);
		}
	}

}
