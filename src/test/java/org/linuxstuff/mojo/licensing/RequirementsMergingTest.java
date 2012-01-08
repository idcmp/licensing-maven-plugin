package org.linuxstuff.mojo.licensing;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.linuxstuff.mojo.licensing.model.ArtifactWithLicenses;
import org.linuxstuff.mojo.licensing.model.LicensingRequirements;

public class RequirementsMergingTest extends AbstractLicensingTest {

	/**
	 * Take three {@code LicensingRequirements} objects, fill them with some
	 * somewhat overlapping disliked licenses, merge them together and ensure
	 * the right number of things come out the other side.
	 */
	@Test
	public void dislikedLicenseMergeTest() {

		LicensingRequirements l1 = new LicensingRequirements();
		LicensingRequirements l2 = new LicensingRequirements();
		LicensingRequirements l3 = new LicensingRequirements();

		l1.addDislikedLicense("disliked1a");
		l1.addDislikedLicense("disliked2a");
		l1.addDislikedLicense("disliked3a");
		l2.addDislikedLicense("disliked1a");
		l2.addDislikedLicense("disliked2b");
		l2.addDislikedLicense("disliked3b");
		l3.addDislikedLicense("disliked1c");
		l3.addDislikedLicense("disliked2c");
		l3.addDislikedLicense("disliked3a");

		LicensingRequirements l = new LicensingRequirements();
		l.combineWith(l1);
		l.combineWith(l2);
		l.combineWith(l3);

		assertEquals(7, l.getDislikedLicenses().size());
	}

	/**
	 * Take three {@code LicensingRequirements} objects, fill them with some
	 * somewhat overlapping dislike exemptions, merge them together and ensure
	 * the right number of things come out the other side.
	 */
	@Test
	public void dislikedExemptionMergeTest() {
		LicensingRequirements l1 = new LicensingRequirements();
		LicensingRequirements l2 = new LicensingRequirements();
		LicensingRequirements l3 = new LicensingRequirements();

		l1.addDislikeExemption("exemption1a");
		l1.addDislikeExemption("exemption2a");
		l1.addDislikeExemption("exemption3a");
		l2.addDislikeExemption("exemption1a");
		l2.addDislikeExemption("exemption2b");
		l2.addDislikeExemption("exemption3b");
		l3.addDislikeExemption("exemption1c");
		l3.addDislikeExemption("exemption2c");
		l3.addDislikeExemption("exemption3a");

		LicensingRequirements l = new LicensingRequirements();
		l.combineWith(l1);
		l.combineWith(l2);
		l.combineWith(l3);

		assertEquals(7, l.getDislikeExemptions().size());

	}

	/**
	 * Take three {@code LicensingRequirements} objects, fill them with some
	 * somewhat overlapping dislike exemptions, merge them together and ensure
	 * the right number of things come out the other side.
	 */
	@Test
	public void artifactWithLicensesMergeTest() {
		LicensingRequirements l1 = new LicensingRequirements();
		LicensingRequirements l2 = new LicensingRequirements();
		LicensingRequirements l3 = new LicensingRequirements();

		ArtifactWithLicenses awl1 = new ArtifactWithLicenses("artifact");

		awl1.addLicense("license1");

		ArtifactWithLicenses awl2 = new ArtifactWithLicenses("artifact");

		awl2.addLicense("license1");
		awl2.addLicense("license2");

		ArtifactWithLicenses awl3 = new ArtifactWithLicenses("artifact");

		awl3.addLicense("license1");
		awl3.addLicense("license2");
		awl3.addLicense("license3");

		l1.addArtifactMissingLicense(awl1);
		l2.addArtifactMissingLicense(awl2);
		l3.addArtifactMissingLicense(awl3);

		LicensingRequirements l = new LicensingRequirements();
		l.combineWith(l1);
		l.combineWith(l2);
		l.combineWith(l3);

		assertEquals(3, l.getLicenseNames("artifact").size());

	}

}
