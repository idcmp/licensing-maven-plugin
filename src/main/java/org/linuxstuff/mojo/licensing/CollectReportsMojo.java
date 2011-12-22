package org.linuxstuff.mojo.licensing;

import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.linuxstuff.mojo.licensing.model.LicensingReport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @goal collect-reports
 * @requiresProject true
 * @aggregator
 * 
 */
public class CollectReportsMojo extends AbstractLicensingMojo {

	/**
	 * The projects in the reactor for aggregation report.
	 * 
	 * @parameter expression="${reactorProjects}"
	 * @readonly
	 * @required
	 * @since 2.8
	 */
	private List<MavenProject> reactorProjects;

	private LicensingReport report;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

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

		for (MavenProject p : reactorProjects) {
			getLog().info("reactor has: " + p + " whose output dir is: " + p.getBuild().getDirectory());
		}

	}

}
