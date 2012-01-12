package org.linuxstuff.mojo.licensing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.License;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.linuxstuff.mojo.licensing.model.ArtifactWithLicenses;

/**
 * Don't worry test, we like you.
 * 
 */
public class DislikedTest extends AbstractLicensingTest {

	/**
	 * With no licensing requirements, no licenses are disliked.
	 */
	@Test
	public void testDislikeWithNoRequirements() {

		TestLicensingMojo mojo = new TestLicensingMojo();

		assertFalse("With no licensing requirements, no licenses are disliked.", mojo.isDisliked(new MavenProject()));

	}

	/**
	 * Projects with multiple licenses where at least one of the licenses is not
	 * disliked are therefore not disliked. That is to say, all licenses for a
	 * project must be disliked for it be considered disliked.
	 */
	@Test
	public void testMultiLicenseNotDisliked() {
		licensingRequirements.addDislikedLicense("disliked-1");
		licensingRequirements.addDislikedLicense("disliked-2");

		License license = new License();
		license.setName("disliked-1");
		mavenProject.getLicenses().add(license);

		mojo.setLicensingRequirements(licensingRequirements);

		assertTrue("A mavenProject with a disliked license is disliked.", mojo.isDisliked(mavenProject));

		license = new License();
		license.setName("disliked-2");
		mavenProject.getLicenses().add(license);

		assertTrue("A mavenProject with all disliked licenses is disliked.", mojo.isDisliked(mavenProject));

		license = new License();
		license.setName("liked!");
		mavenProject.getLicenses().add(license);

		assertFalse("A mavenProject with at least one liked license is liked.", mojo.isDisliked(mavenProject));

	}

	/**
	 * If a maven project has one license and that license is configured to be
	 * disliked, then the project is disliked.
	 */
	@Test
	public void testSimpleDisliked() {

		licensingRequirements.addDislikedLicense("disliked");

		License license = new License();
		license.setName("disliked");

		mavenProject.getLicenses().add(license);

		mojo.setLicensingRequirements(licensingRequirements);

		assertTrue("An mavenProject with a disliked license is disliked.", mojo.isDisliked(mavenProject));

	}

	/**
	 * A project with a disliked license but whose GAV make it exempt from being
	 * disliked is therefore liked.
	 */
	@Test
	public void testDislikeExemption() {

		License license = new License();
		license.setName("disliked");

		List<License> licenses = new ArrayList<License>();
		licenses.add(license);

		mavenProject.setLicenses(licenses);

		mojo.setLicensingRequirements(licensingRequirements);

		licensingRequirements.addDislikedLicense("disliked");
		assertTrue("An mavenProject with a disliked license is disliked.", mojo.isDisliked(mavenProject));

		licensingRequirements.addDislikeExemption(mavenProject.getId());

		assertFalse("An mavenProject with a disliked license that is exempt from being disliked, is therefore not disliked.", mojo.isDisliked(mavenProject));

	}

	/**
	 * A maven project which doesn't have a license, but has a license added in
	 * licensing requirements and that license is disliked, is therefore
	 * disliked.
	 */
	@Test
	public void testConfiguredDislike() {

		ArtifactWithLicenses awl = new ArtifactWithLicenses(mavenProject.getId());
		awl.addLicense("disliked");

		licensingRequirements.addArtifactMissingLicense(awl);

		assertFalse("This project must not be disliked.", mojo.isDisliked(mavenProject));

		licensingRequirements.addDislikedLicense("disliked");

		assertTrue("This project must be disliked.", mojo.isDisliked(mavenProject));

	}

}
