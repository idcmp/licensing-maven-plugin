package org.linuxstuff.mojo.licensing.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LicensingRequirements {

	/**
	 * Map's key is {@code MavenProject#getId()}, its value is a {@code Set} of
	 * applicable licenses as would normally found in {@code License#getName()}
	 */
	private Map<String, Set<String>> missingLicenses;

	/**
	 * The key is the "wrong" license name (ala {@code License#getName()}, the
	 * value of this map is what we would like the license name to be.
	 */
	private Map<String, String> coalescedLicenses;

	/**
	 * Set of disliked licenses. These are matched based on
	 * {@code License#getName()}.
	 */
	private Set<String> dislikedLicenses;

	/**
	 * Set of artifacts (ala {@code MavenProject#getId()}) that we will exempt
	 * from failing "dislike" checks.
	 */

	private Set<String> dislikeExemptions;

	public LicensingRequirements() {
		missingLicenses = new HashMap<String, Set<String>>();
		coalescedLicenses = new HashMap<String, String>();
		dislikedLicenses = new HashSet<String>();
		dislikeExemptions = new HashSet<String>();
	}

	public void addMissingLicense(String id, String licenseName) {

		if (!missingLicenses.containsKey(id)) {
			missingLicenses.put(id, new HashSet<String>());
		}
		missingLicenses.get(id).add(licenseName);

	}

	/**
	 * Returns a set of <i>additional</i> licenses applicable for the given
	 * artifact id. Will return an empty set if no licenses are defined in the
	 * license requirements XML. This is primarily used to handle the situation
	 * when an artifact does not define a license block.
	 * 
	 * @param id
	 *            artifact id ala {@code MavenProject#getId()}.
	 * @return a set of license names; already run through
	 *         {@code #getCorrectLicenseName(String)}.
	 */
	public Set<String> getLicenseNames(String id) {
		Set<String> licenses = new HashSet<String>();
		if (missingLicenses.containsKey(id)) {
			for (String licenseName : missingLicenses.get(id)) {
				licenses.add(getCorrectLicenseName(licenseName));
			}
		}

		return licenses;
	}

	public void addLicenseAka(String licenseName, String wrongLicenseName) {
		coalescedLicenses.put(wrongLicenseName, licenseName);
	}

	public String getCorrectLicenseName(String licenseName) {
		if (coalescedLicenses.containsKey(licenseName)) {
			return coalescedLicenses.get(licenseName);
		} else {
			return licenseName;
		}
	}

	public void addDislikedLicense(String disliked) {
		dislikedLicenses.add(disliked);
	}

	public boolean isDislikedLicense(String licenseName) {
		return dislikedLicenses.contains(licenseName);
	}

	public void addDislikedExemption(String id) {
		dislikeExemptions.add(id);
	}

	public boolean isDislikedExempt(String id) {
		return dislikeExemptions.contains(id);
	}
}
