package org.linuxstuff.mojo.licensing.model;

import java.util.HashSet;
import java.util.Set;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("artifact")
public class ArtifactWithLicenses {

	@XStreamAsAttribute
	@XStreamAlias("id")
	private String artifactId;

	@XStreamAsAttribute
	@XStreamAlias("name")
	private String name;

	@XStreamImplicit(itemFieldName = "license")
	private Set<String> licenses;

	public ArtifactWithLicenses() {
		licenses = new HashSet<String>();
	}

	public ArtifactWithLicenses(String artifactId) {
		this.artifactId = artifactId;
		this.licenses = new HashSet<String>();
	}

	public ArtifactWithLicenses(String artifactId, Set<String> licenses) {
		this.artifactId = artifactId;
		this.licenses = licenses;
	}

	public void combineWith(ArtifactWithLicenses other) {
		licenses.addAll(other.getLicenses());
	}

	public String getArtifactId() {
		return artifactId;
	}

	public Set<String> getLicenses() {
		return licenses;
	}

	public void addLicense(String license) {
		licenses.add(license);
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public void setLicenses(Set<String> licenses) {
		this.licenses = licenses;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((artifactId == null) ? 0 : artifactId.hashCode());
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
		ArtifactWithLicenses other = (ArtifactWithLicenses) obj;
		if (artifactId == null) {
			if (other.artifactId != null)
				return false;
		} else if (!artifactId.equals(other.artifactId))
			return false;
		return true;
	}

}
