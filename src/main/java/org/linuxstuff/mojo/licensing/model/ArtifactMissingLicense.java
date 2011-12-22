package org.linuxstuff.mojo.licensing.model;

import java.util.HashSet;
import java.util.Set;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("artifact")
public class ArtifactMissingLicense {

	@XStreamAsAttribute
	@XStreamAlias("id")
	private String artifactId;

	@XStreamImplicit(itemFieldName = "license")
	private Set<String> licenses;

	public ArtifactMissingLicense() {
		licenses = new HashSet<String>();
	}

	public ArtifactMissingLicense(String artifactId, Set<String> licenses) {
		this.artifactId = artifactId;
		this.licenses = licenses;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public Set<String> getLicenses() {
		return licenses;
	}

}
