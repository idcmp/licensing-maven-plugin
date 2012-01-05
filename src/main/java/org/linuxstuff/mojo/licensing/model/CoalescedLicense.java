package org.linuxstuff.mojo.licensing.model;

import java.util.Set;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("license")
public class CoalescedLicense {

	@XStreamAsAttribute
	@XStreamAlias("name")
	private String finalName;

	@XStreamImplicit(itemFieldName = "aka")
	private Set<String> otherNames;

	public CoalescedLicense(String finalName, Set<String> otherNames) {
		this.finalName = finalName;
		this.otherNames = otherNames;
	}

	public String getFinalName() {
		return finalName;
	}

	public Set<String> getOtherNames() {
		return otherNames;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((finalName == null) ? 0 : finalName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CoalescedLicense other = (CoalescedLicense) obj;
		if (finalName == null) {
			if (other.finalName != null)
				return false;
		} else if (!finalName.equals(other.finalName))
			return false;
		return true;
	}

	/**
	 * This method does not attempt to do anything creative with cascading final
	 * names or anything. It simply combines the "other names" of the other
	 * {@code CoalescedLicense}.
	 */
	public void combineWith(CoalescedLicense other) {
		otherNames.addAll(other.getOtherNames());
	}

}
