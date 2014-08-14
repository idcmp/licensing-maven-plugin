package org.linuxstuff.mojo.licensing;

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.linuxstuff.mojo.licensing.model.ArtifactWithLicenses;
import org.linuxstuff.mojo.licensing.model.LicensingReport;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * @goal collect-reports
 * @requiresProject true
 * @threadSafe
 * @aggregator
 */
public class CollectReportsMojo extends AbstractLicensingMojo {

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
     * @parameter property="reactorProjects"
     * @readonly
     * @required
     */
    private List<MavenProject> reactorProjects;

    private LicensingReport report;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        readLicensingRequirements();

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
        report.writeReport(outputFile);

        projectHelper.attachArtifact(project, outputFile, "aggregated-third-party-licensing");

    }

}
