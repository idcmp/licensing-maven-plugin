package org.linuxstuff.mojo.licensing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


import java.util.Arrays;
import java.util.HashSet;

import org.apache.maven.model.License;
import org.junit.Test;
import org.linuxstuff.mojo.licensing.model.ArtifactWithLicenses;

public class LicenseCollectionTest extends AbstractLicensingTest {

	/**
	 * Simple to test to make sure licenses in maven projects are found.
	 */
	@Test
	public void testMavenLicensesAreFound() {
		License license = new License();
		license.setName("name");

		assertEquals(0, mojo.collectLicensesForMavenProject(mavenProject).size());
		assertFalse(mojo.hasLicense(mavenProject));

		mavenProject.getLicenses().add(license);
		assertEquals(1, mojo.collectLicensesForMavenProject(mavenProject).size());
		assertTrue(mojo.hasLicense(mavenProject));

	}

	/**
	 * Simple test to make sure that licenses specified in license requirements
	 * are found.
	 */
	@Test
	public void testThatManualLicensesAreFound() {

		assertEquals(0, mojo.collectLicensesForMavenProject(mavenProject).size());

		ArtifactWithLicenses awl = new ArtifactWithLicenses(mavenProject.getId());
		licensingRequirements.addArtifactMissingLicense(awl);

		assertEquals(0, mojo.collectLicensesForMavenProject(mavenProject).size());

		awl.addLicense("hello1");
		awl.addLicense("hello2");
		awl.addLicense("hello3");

		assertEquals(3, mojo.collectLicensesForMavenProject(mavenProject).size());
		assertTrue(mojo.hasLicense(mavenProject));

	}

	/**
	 * If we list licenses in licensing requirements *and* the project has
	 * listed a license, the one in the project wins.
	 */
	@Test
	public void testThatEmbeddedLicensesWin() {
		License license = new License();
		license.setName("embedded");

		assertEquals(0, mojo.collectLicensesForMavenProject(mavenProject).size());

		mavenProject.getLicenses().add(license);
		assertEquals(1, mojo.collectLicensesForMavenProject(mavenProject).size());

		ArtifactWithLicenses awl = new ArtifactWithLicenses(mavenProject.getId());
		awl.addLicense("hello1");
		awl.addLicense("hello2");
		awl.addLicense("hello3");

		licensingRequirements.addArtifactMissingLicense(awl);

		assertEquals(1, mojo.collectLicensesForMavenProject(mavenProject).size());
		assertEquals("embedded", mojo.collectLicensesForMavenProject(mavenProject).iterator().next());
		assertTrue(mojo.hasLicense(mavenProject));

	}

	/**
	 * If we list licenses in licensing requirements *and* the project has
	 * listed a license, but the project's licenses have no name (xbean-asm4-shaded I'm looking at you!)
	 * the one in the licensing requirements are used as a fallback.
	 */
	@Test
	public void testFallbackToLicensingRequirementsIfEmbeddedLicensesAreMalformed() {
		License unnamedLicense = new License();
		unnamedLicense.setName("");
		License nullNamedLicense = new License();
		nullNamedLicense.setName(null);

		mavenProject.getLicenses().add(unnamedLicense);
		mavenProject.getLicenses().add(nullNamedLicense);

		ArtifactWithLicenses awl = new ArtifactWithLicenses(mavenProject.getId());
		awl.addLicense("hello1");
		awl.addLicense("hello2");
		awl.addLicense("hello3");

		licensingRequirements.addArtifactMissingLicense(awl);

		assertEquals("License Collector should fallback to licensing requirements block if licenses are invalid",
				new HashSet<String>(Arrays.asList("hello1", "hello2", "hello3")),
				mojo.collectLicensesForMavenProject(mavenProject));
		assertTrue(mojo.hasLicense(mavenProject));

	}
}
