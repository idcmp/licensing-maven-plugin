package org.linuxstuff.mojo.licensing;

import java.util.ArrayList;

import org.apache.maven.model.License;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.linuxstuff.mojo.licensing.model.LicensingRequirements;

public abstract class AbstractLicensingTest {

    protected LicensingRequirements licensingRequirements;
    protected MavenProject mavenProject;
    protected TestLicensingMojo mojo;

    class TestLicensingMojo extends AbstractLicensingMojo {

        @Override
        public void execute() throws MojoExecutionException, MojoFailureException {

        }

        public void setLicensingRequirements(LicensingRequirements requirements) {
            licensingRequirements = requirements;
        }
    }

    @Before
    public void setUp() {
        licensingRequirements = new LicensingRequirements();

        mavenProject = new MavenProject();
        Model model = mavenProject.getModel();
        model.setGroupId("groupdId");
        model.setArtifactId("artifactId");
        model.setVersion("1.0");
        model.setPackaging("jar");
        model.setUrl("http://linuxstuff.org/for-testing");

        mavenProject.setLicenses(new ArrayList<License>());

        mojo = new TestLicensingMojo();
        mojo.setLicensingRequirements(licensingRequirements);

    }

}
