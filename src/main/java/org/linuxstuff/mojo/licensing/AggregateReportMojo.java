package org.linuxstuff.mojo.licensing;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.linuxstuff.mojo.licensing.model.LicensingReport;

/**
 * @goal aggregate
 * @requiresDependencyResolution test
 * @requiresProject true
 * @aggregator true
 */
public class AggregateReportMojo extends CheckMojo {

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
	 */
	private List<MavenProject> reactorProjects;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		readLicensingRequirements();

		LicensingReport bigReport = new LicensingReport();

		for (MavenProject project : reactorProjects) {

			LicensingReport report = generateReport(project);

			bigReport.combineWith(report);

		}

		File file = new File(project.getBuild().getDirectory(), aggregatedThirdPartyLicensingFilename);

		bigReport.writeReport(file);

	}

}
