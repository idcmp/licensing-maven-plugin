package org.linuxstuff.mojo.licensing.model;

import java.util.HashSet;
import java.util.Set;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("licensing-requirements")
public class LicensingRequirements {

	@XStreamAlias("missing-licenses")
	private Set<ArtifactMissingLicense> missingLicenses;

	@XStreamAlias("coalesced-licenses")
	private Set<CoalescedLicense> coalescedLicenses;

	@XStreamAlias("disliked-licenses")
	@XStreamImplicit(itemFieldName = "disliked-license")
	private Set<String> dislikedLicenses;

	@XStreamAlias("dislike-exemptions")
	@XStreamImplicit(itemFieldName = "dislike-exemption")
	private Set<String> dislikeExemptions;

	public LicensingRequirements() {
		missingLicenses = new HashSet<ArtifactMissingLicense>();
		coalescedLicenses = new HashSet<CoalescedLicense>();
		dislikedLicenses = new HashSet<String>();
		dislikeExemptions = new HashSet<String>();
	}

	public void addArtifactMissingLicense(ArtifactMissingLicense missingLicense) {
		missingLicenses.add(missingLicense);
	}

	public void addCoalescedLicense(CoalescedLicense coalescedLicense) {
		coalescedLicenses.add(coalescedLicense);
	}

	public void addDislikedLicense(String licenseName) {
		dislikedLicenses.add(licenseName);
	}

	public void addDislikeExemption(String artifactId) {
		dislikeExemptions.add(artifactId);
	}

	public boolean isDislikedLicense(String license) {
		return dislikedLicenses.contains(license);
	}

	public String getCorrectLicenseName(String name) {
		for (CoalescedLicense coalesced : coalescedLicenses) {
			if (coalesced.getFinalName().equals(name))
				return name;
			for (String otherName : coalesced.getOtherNames()) {
				if (otherName.equalsIgnoreCase(name))
					return coalesced.getFinalName();
			}
		}

		return name;
	}

	public boolean isExemptFromDislike(String artifactId) {
		return dislikeExemptions.contains(artifactId);
	}

	public Set<String> getLicenseNames(String id) {
		Set<String> licenses = new HashSet<String>();
		for (ArtifactMissingLicense missing : missingLicenses) {
			if (missing.getArtifactId().equals(id)) {
				licenses.addAll(missing.getLicenses());
			}
		}

		return licenses;
	}
}
