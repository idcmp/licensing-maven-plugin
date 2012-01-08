package org.linuxstuff.mojo.licensing;

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.linuxstuff.mojo.licensing.model.LicensingReport;

/**
 * Aggregate mojo. Will walk your reactor building in memory licensing reports
 * making them into one giant report. This mojo <b>does not</b> check for
 * missing or disliked artifacts (use {@code CheckMojo} for that).
 * 
 * @goal aggregate
 * @requiresDependencyResolution test
 * @requiresProject true
 * @aggregator
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

		if (!project.isExecutionRoot()) {
			getLog().debug(project.getId() + " is not execution root, not making aggregated licensing report.");
			return;
		}

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
