package org.linuxstuff.mojo.licensing;

import org.apache.maven.plugin.MojoFailureException;
import org.junit.Test;
import org.linuxstuff.mojo.licensing.model.ArtifactWithLicenses;
import org.linuxstuff.mojo.licensing.model.LicensingReport;

public class CheckForFailureTest {

	/**
	 * Ignore all our problems.
	 */
	@Test
	public void testIgnoreEverything() throws MojoFailureException {

		LicensingReport report = new LicensingReport();

		CheckMojo mojo = new CheckMojo();

		mojo.failIfDisliked = false;
		mojo.failIfMissing = false;

		report.addMissingLicense(new ArtifactWithLicenses("missing"));
		report.addDislikedArtifact(new ArtifactWithLicenses("disliked"));

		mojo.checkForFailure(report);
	}

	/**
	 * Blow up because of the disliked artifact.
	 */
	@Test(expected = MojoFailureException.class)
	public void testDislkedThrowsException() throws MojoFailureException {
		LicensingReport report = new LicensingReport();

		CheckMojo mojo = new CheckMojo();

		mojo.failIfDisliked = true;
		mojo.failIfMissing = false;

		report.addDislikedArtifact(new ArtifactWithLicenses("disliked"));

		mojo.checkForFailure(report);

	}

	/**
	 * Blow up because of the artifact with a missing license.
	 */
	@Test(expected = MojoFailureException.class)
	public void testMissingThrowsException() throws MojoFailureException {
		LicensingReport report = new LicensingReport();

		CheckMojo mojo = new CheckMojo();

		mojo.failIfDisliked = false;
		mojo.failIfMissing = true;

		report.addMissingLicense(new ArtifactWithLicenses("disliked"));

		mojo.checkForFailure(report);

	}

	/**
	 * Don't blow up about the disliked artifact (but enable blowing up for
	 * artifacts missing licenses).
	 */
	@Test
	public void testIgnoreDisliked() throws MojoFailureException {

		LicensingReport report = new LicensingReport();

		CheckMojo mojo = new CheckMojo();

		mojo.failIfDisliked = false;
		mojo.failIfMissing = true;

		report.addDislikedArtifact(new ArtifactWithLicenses("disliked"));

		mojo.checkForFailure(report);
	}

	/**
	 * Don't blow up about artifacts missing licenses (but enable blowing up for
	 * disliked licenses).
	 */
	@Test
	public void testIgnoreMissing() throws MojoFailureException {

		LicensingReport report = new LicensingReport();

		CheckMojo mojo = new CheckMojo();

		mojo.failIfDisliked = true;
		mojo.failIfMissing = false;

		report.addMissingLicense(new ArtifactWithLicenses("missing"));

		mojo.checkForFailure(report);
	}

	/**
	 * Also blow up if *everything* is bad. Technically the user sees a
	 * different message, but I'm not about to scrape an exception message.
	 */
	@Test(expected = MojoFailureException.class)
	public void testEverythingIsBad() throws MojoFailureException {

		LicensingReport report = new LicensingReport();

		CheckMojo mojo = new CheckMojo();

		mojo.failIfDisliked = true;
		mojo.failIfMissing = true;

		report.addMissingLicense(new ArtifactWithLicenses("missing"));
		report.addDislikedArtifact(new ArtifactWithLicenses("disliked"));

		mojo.checkForFailure(report);
	}

}
