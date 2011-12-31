package org.linuxstuff.mojo.licensing;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.apache.maven.model.License;
import org.junit.Before;
import org.junit.Test;
import org.linuxstuff.mojo.licensing.model.ArtifactWithLicenses;
import org.linuxstuff.mojo.licensing.model.CoalescedLicense;

public class LicenseCoalescingTest extends AbstractLicensingTest {

	private CoalescedLicense coalescedLicense;

	@Before
	public void makeSimpleCoalescedLicense() {
		Set<String> aka = new HashSet<String>();
		aka.add("finalName");
		aka.add("final Name ");
		aka.add("FINAL NAME ");

		coalescedLicense = new CoalescedLicense("Final Name v1.0", aka);

	}

	/**
	 * A simple test to ensure that looking for a license name has spaces
	 * trimmed and is case insensitive. You clod.
	 */
	@Test
	public void simpleCoalescedTest() {

		licensingRequirements.addCoalescedLicense(coalescedLicense);

		assertEquals("Final Name v1.0", licensingRequirements.getCorrectLicenseName("Final Name v1.0"));
		assertEquals("Final Name v1.0", licensingRequirements.getCorrectLicenseName("finalname"));
		assertEquals("Final Name v1.0", licensingRequirements.getCorrectLicenseName("   fINAlname   "));
		assertEquals("Final Name v1.0", licensingRequirements.getCorrectLicenseName(" finalname "));
		assertEquals("GPL", licensingRequirements.getCorrectLicenseName("GPL"));

	}

	/**
	 * Ensure that embedded licenses are handled.
	 */
	@Test
	public void testThatEmbeddedLicensesAreCoalesced() {
		licensingRequirements.addCoalescedLicense(coalescedLicense);

		License license = new License();
		license.setName(" fInAlNamE ");
		mavenProject.getLicenses().add(license);

		Set<String> licenses = mojo.collectLicensesForMavenProject(mavenProject);

		assertEquals(1, licenses.size());

		assertEquals("Final Name v1.0", licenses.iterator().next());

	}

	/**
	 * Ensure that the licenses in licensing requirements are also coalesced.
	 */
	@Test
	public void testThatConfiguredLicensesAreCoalesced() {

		licensingRequirements.addCoalescedLicense(coalescedLicense);

		ArtifactWithLicenses awl = new ArtifactWithLicenses(mavenProject.getId());
		awl.addLicense("finalname");

		licensingRequirements.addArtifactMissingLicense(awl);

		Set<String> licenses = mojo.collectLicensesForMavenProject(mavenProject);

		assertEquals(1, licenses.size());

		assertEquals("Final Name v1.0", licenses.iterator().next());

	}

}
