package org.linuxstuff.mojo.licensing;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
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

	/**
	 * Fail the build if any dependencies are either under disliked licenses or
	 * are missing licensing information.
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {

		int dislikedArtifacts = 0, missingLicense = 0;
		readLicensingRequirements();

		Document doc = createDocument();
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
				getLog().warn("Licensing: The artifact " + mavenProject.getId() + " is under a disliked license.");
				dislikedArtifacts++;
				disliked.appendChild(artifact.cloneNode(true));
			}

			artifacts.appendChild(artifact);

		}

		root.setAttribute("missing-licenses", "" + missingLicense);
		root.setAttribute("disliked-licenses", "" + dislikedArtifacts);

		if (dislikedArtifacts > 0 && missingLicense > 0 && failIfDisliked && failIfMissing) {
			root.setAttribute(LICENSING_CHECK, "fail");
			writeDocument(doc);
			throw new MojoFailureException("This project has " + dislikedArtifacts + " disliked artifact"
					+ ((dislikedArtifacts == 1) ? "" : "s") + " and " + missingLicense + " artifact"
					+ ((missingLicense == 1) ? "" : "s") + " missing licensing information.");
		} else if (missingLicense > 0 && failIfMissing) {
			root.setAttribute(LICENSING_CHECK, "fail");
			writeDocument(doc);
			throw new MojoFailureException("This project has " + missingLicense + " artifact"
					+ ((missingLicense == 1) ? "" : "s") + " missing licensing information.");
		} else if (dislikedArtifacts > 0 && failIfDisliked) {
			root.setAttribute(LICENSING_CHECK, "fail");
			writeDocument(doc);
			throw new MojoFailureException("This project has " + dislikedArtifacts + " disliked artifact"
					+ ((dislikedArtifacts == 1) ? "" : "s") + ".");
		}

		root.setAttribute(LICENSING_CHECK, "pass");
		writeDocument(doc);

	}

	private void writeDocument(Document doc) throws MojoExecutionException {
		try {
			getLog().info("Time to write out...");
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File("kapow.xml"));
			transformer.transform(source, result);
		} catch (Exception e) {
			throw new MojoExecutionException("Failed to write out XML document.", e);
		}
	}

	private Document createDocument() throws MojoExecutionException {
		try {

			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			return doc;
		} catch (Exception e) {
			throw new MojoExecutionException("Failed to initialize XML", e);
		}
	}
}
