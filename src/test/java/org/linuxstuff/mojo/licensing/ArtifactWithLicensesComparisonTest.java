package org.linuxstuff.mojo.licensing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.linuxstuff.mojo.licensing.model.ArtifactWithLicenses;

public class ArtifactWithLicensesComparisonTest {

	/**
	 * Comparisons for {@code ArtifactWithLicenses} are done solely on the
	 * artifactId. This way the artifact only shows up once in {@code Set}s.
	 */
	@Test
	public void test() {
		ArtifactWithLicenses awl1 = new ArtifactWithLicenses("artifactA");
		ArtifactWithLicenses awl2 = new ArtifactWithLicenses("artifactB");

		ArtifactWithLicenses awl3 = new ArtifactWithLicenses("artifactA");

		assertFalse("ArtifactWithLicenses are compared only on artifactId.", awl1.equals(awl2));
		assertTrue("ArtifactWithLicenses are compared only on artifactId.", awl1.equals(awl3));

		awl1.addLicense("one two");
		awl2.addLicense("one two");

		assertFalse("ArtifactWithLicenses are compared only on artifactId.", awl1.equals(awl2));
		assertTrue("ArtifactWithLicenses are compared only on artifactId.", awl1.equals(awl3));

	}

}
