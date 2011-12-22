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

}
