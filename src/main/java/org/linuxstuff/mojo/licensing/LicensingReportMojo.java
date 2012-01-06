package org.linuxstuff.mojo.licensing;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.linuxstuff.mojo.licensing.model.ArtifactWithLicenses;
import org.linuxstuff.mojo.licensing.model.LicensingReport;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * @goal licensing-report
 * @phase site
 */
public class LicensingReportMojo extends AbstractMavenReport {

	/**
	 * Maven ProjectHelper.
	 * 
	 * @component
	 * @readonly
	 */
	private MavenProjectHelper projectHelper;

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

	/**
	 * The name of the the XML file which contains licensing information one artifact.
	 * 
	 * @parameter expression="${thirdPartyLicensingFilename}" default-value="third-party-licensing.xml"
	 */
	protected String thirdPartyLicensingFilename;

	/**
	 * The name of the the XML file which contains the aggregated licensing information for artifacts.
	 * 
	 * @parameter expression="${aggregatedThirdPartyLicensingFilename}" default-value="aggregated-third-party-licensing.xml"
	 */
	protected String aggregatedThirdPartyLicensingFilename;

	/**
	 * Directory where reports will go.
	 * 
	 * @parameter expression="${project.reporting.outputDirectory}"
	 * @required
	 * @readonly
	 */
	private String outputDirectory;

	/**
	 * @parameter default-value="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject project;

	/**
	 * @component
	 * @required
	 * @readonly
	 */
	private Renderer siteRenderer;

	@Override
	public String getDescription(Locale locale) {
		return getBundle(locale).getString("report.licensing-report.description");
	}

	@Override
	public String getName(Locale locale) {
		return getBundle(locale).getString("report.licensing-report.name");
	}

	@Override
	public String getOutputName() {
		return "licensing-report";
	}

	@Override
	protected String getOutputDirectory() {
		return outputDirectory;
	}

	@Override
	protected MavenProject getProject() {
		return project;
	}

	private ResourceBundle getBundle(Locale locale) {
		return ResourceBundle.getBundle("licensing-report", locale, this.getClass().getClassLoader());
	}

	@Override
	protected void executeReport(Locale locale) throws MavenReportException {

		if (!shouldGenerate()) {
			return;
		}

		report = new LicensingReport();

		XStream xstream = new XStream(new StaxDriver());
		xstream.processAnnotations(ArtifactWithLicenses.class);
		xstream.processAnnotations(LicensingReport.class);

		for (MavenProject p : reactorProjects) {

			File licenseXml = new File(p.getBuild().getDirectory(), thirdPartyLicensingFilename);

			if (licenseXml.canRead()) {
				LicensingReport artifactReport = (LicensingReport) xstream.fromXML(licenseXml);
				getLog().debug("Successfully turned " + licenseXml + " into " + artifactReport);
				report.combineWith(artifactReport);
			} else {
				getLog().debug("No report file found at: " + licenseXml.getAbsolutePath());
			}
		}

		File outputFile = new File(project.getBuild().getDirectory(), aggregatedThirdPartyLicensingFilename);
		try {
			report.writeReport(outputFile);
		} catch (MojoExecutionException e) {
			throw new MavenReportException("Error while writing report.", e);
		}

		projectHelper.attachArtifact(project, outputFile, "aggregated-third-party-licensing");

	}

	protected boolean shouldGenerate() {
		if (project.isExecutionRoot()) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isExternalReport() {
		return true;
	}

	@Override
	protected Renderer getSiteRenderer() {
		return siteRenderer;
	}

}
