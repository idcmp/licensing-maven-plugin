package org.linuxstuff.mojo.licensing;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.License;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.resource.ResourceManager;
import org.linuxstuff.mojo.licensing.model.ArtifactWithLicenses;
import org.linuxstuff.mojo.licensing.model.CoalescedLicense;
import org.linuxstuff.mojo.licensing.model.LicensingRequirements;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * Some basic plumbing for licensing mojos. I've borrowed
 * {@code MavenProjectDependenciesConfigurator} from the license plugin since it
 * rocks.
 * 
 * @see CheckMojo
 * @see CollectReportsMojo
 */
abstract public class AbstractLicensingMojo extends AbstractMojo implements MavenProjectDependenciesConfigurator {

	/**
	 * Used to read in the licensing-requirements from the plugin's classpath.
	 * 
	 * @plexus.requirement role="org.codehaus.plexus.resource.ResourceManager"
	 *                     role-hint="default"
	 * @component
	 * @required
	 * @readonly
	 * @since 1.0
	 */
	private ResourceManager locator;

	/**
	 * Local Repository.
	 * 
	 * @parameter expression="${localRepository}"
	 * @required
	 * @readonly
	 * @since 1.0
	 */
	protected ArtifactRepository localRepository;

	/**
	 * Remote repositories used for the project.
	 * 
	 * @parameter expression="${project.remoteArtifactRepositories}"
	 * @required
	 * @readonly
	 * @since 1.0
	 */
	protected List remoteRepositories;

	/**
	 * Location of the file.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 * @readOnly
	 * @since 1.0
	 */
	private File outputDirectory;

	/**
	 * The Maven Project Object.
	 * 
	 * @parameter default-value="${project}"
	 * @required
	 * @readonly
	 * @since 1.0
	 */
	protected MavenProject project;

	/**
	 * A {@code DependenciesTool} as borrowed from the licensing-maven-plugin.
	 * 
	 * @component
	 * @readonly
	 * @since 1.0
	 */
	private DependenciesTool dependenciesTool;

	/**
	 * A filter to exclude some scopes.
	 * 
	 * @parameter expression="${licensing.excludedScopes}"
	 *            default-value="system"
	 * @since 1.0
	 */
	protected String excludedScopes;

	/**
	 * A filter to include only some scopes, if let empty then all scopes will
	 * be used (no filter).
	 * 
	 * @parameter expression="${licensing.includedScopes}" default-value=""
	 * @since 1.0
	 */
	protected String includedScopes;

	/**
	 * A filter to exclude some GroupIds
	 * 
	 * @parameter expression="${licensing.excludedGroups}" default-value=""
	 * @since 1.0
	 */
	protected String excludedGroups;

	/**
	 * The name of the the XML file which contains licensing information one
	 * artifact.
	 * 
	 * @parameter expression="${thirdPartyLicensingFilename}"
	 *            default-value="third-party-licensing.xml"
	 */
	protected String thirdPartyLicensingFilename;

	/**
	 * The name of the the XML file which contains the aggregated licensing
	 * information for artifacts.
	 * 
	 * @parameter expression="${aggregatedThirdPartyLicensingFilename}"
	 *            default-value="aggregated-third-party-licensing.xml"
	 */
	protected String aggregatedThirdPartyLicensingFilename;

	/**
	 * A filter to include only some GroupIds
	 * 
	 * @parameter expression="${licensing.includedGroups}" default-value=""
	 * @since 1.0
	 */
	protected String includedGroups;

	/**
	 * A filter to exclude some ArtifactsIds
	 * 
	 * @parameter expression="${licensing.excludedArtifacts}" default-value=""
	 * @since 1.0
	 */
	protected String excludedArtifacts;

	/**
	 * A filter to include only some ArtifactsIds
	 * 
	 * @parameter expression="${licensing.includedArtifacts}" default-value=""
	 * @since 1.0
	 */
	protected String includedArtifacts;

	/**
	 * Include transitive dependencies when downloading license files.
	 * 
	 * @parameter default-value="true"
	 * @since 1.0
	 */
	protected boolean includeTransitiveDependencies;

	/**
	 * Location of license requirement XML files.
	 * 
	 * @parameter
	 * @since 1.0
	 */
	protected List<String> licensingRequirementFiles;

	protected LicensingRequirements licensingRequirements = new LicensingRequirements();

	/**
	 * Build a list of artifacts that this project depends on, but resolves them
	 * into {@code MavenProject}s so we can look at their {@code License}
	 * information. Honours all the include/exclude parameters above.
	 * 
	 * @return Does not return null, will return an empty set.
	 */
	protected Collection<MavenProject> getProjectDependencies() {

		Map<String, MavenProject> dependencies = dependenciesTool.loadProjectDependencies(project, this, localRepository, remoteRepositories, null);

		return dependencies.values();

	}

	/**
	 * Swallow an XML file with licensing requirements. See the
	 * {@code LicensingRequirements} model for more details.
	 * 
	 * @throws MojoExecutionException wrapping original exceptions
	 */
	protected void readLicensingRequirements() throws MojoExecutionException {

		XStream xstream = new XStream(new StaxDriver());

		xstream.processAnnotations(LicensingRequirements.class);
		xstream.processAnnotations(ArtifactWithLicenses.class);
		xstream.processAnnotations(CoalescedLicense.class);

		if (licensingRequirementFiles == null) {
			getLog().debug("No licensing requirement files specified.");
			return;
		}

		List<LicensingRequirements> requirements = new ArrayList<LicensingRequirements>();

		for (String requirementsFile : licensingRequirementFiles) {

			try {
				requirements.add((LicensingRequirements) xstream.fromXML(locator.getResourceAsInputStream(requirementsFile)));

			} catch (Exception e) {
				throw new MojoExecutionException("Could not read licensing requirements file: " + requirementsFile, e);
			}
		}

		this.licensingRequirements = mergeLicenseRequirements(requirements);
	}

	/**
	 * Plugin does not yet support merging {@code LicensingRequirements}, but
	 * when it does, this is where it will do it.
	 */
	private LicensingRequirements mergeLicenseRequirements(List<LicensingRequirements> requirements) {

		if (!requirements.isEmpty()) {
			return requirements.get(0);
		}
		return new LicensingRequirements();

	}

	/**
	 * As long as the {@code MavenProject} is under at least one liked license,
	 * then it is liked. This method will also consider licensing specified in
	 * licensing requirements; but only if the {@code MavenProject} does not
	 * have its own {@code License} block. If no licensing at all is found then
	 * it is considered disliked.
	 */
	protected boolean isDisliked(MavenProject mavenProject) {

		if (!licensingRequirements.containsDislikedLicenses()) {
			return false;
		}

		if (licensingRequirements.isExemptFromDislike(mavenProject.getId())) {
			return false;
		}

		Set<String> licenses = collectLicensesForMavenProject(mavenProject);

		for (String license : licenses) {

			if (!licensingRequirements.isDislikedLicense(license))
				return false;
		}

		return true;
	}

	protected boolean hasLicense(MavenProject mavenProject) {
		return !collectLicensesForMavenProject(mavenProject).isEmpty();
	}

	protected Set<String> collectLicensesForMavenProject(MavenProject mavenProject) {
		Set<String> licenses = new HashSet<String>();

		/**
		 * If an artifact declares a license, we will use it <b>instead</b> of
		 * anything defined in licensing requirements.
		 */
		if (mavenProject.getLicenses() != null && mavenProject.getLicenses().size() > 0) {
			getLog().debug("Licensing: " + mavenProject.getId() + " has licensing information in it.");

			List<License> embeddedLicenses = (List<License>) mavenProject.getLicenses();
			for (License license : embeddedLicenses) {
				if (license.getName() != null) {
					licenses.add(licensingRequirements.getCorrectLicenseName(license.getName()));
				}
			}

		} else {
			Set<String> hardcodedLicenses = licensingRequirements.getLicenseNames(mavenProject.getId());
			for (String license : hardcodedLicenses) {
				licenses.add(licensingRequirements.getCorrectLicenseName(license));
			}
		}

		return licenses;

	}

	public boolean isIncludeTransitiveDependencies() {
		return includeTransitiveDependencies;
	}

	public List<String> getIncludedScopes() {
		String[] split = includedScopes == null ? new String[0] : includedScopes.split(",");
		return Arrays.asList(split);
	}

	public List<String> getExcludedScopes() {
		String[] split = excludedScopes == null ? new String[0] : excludedScopes.split(",");
		return Arrays.asList(split);
	}

	public String getIncludedArtifacts() {
		return includedArtifacts;
	}

	public String getIncludedGroups() {
		return includedGroups;
	}

	public String getExcludedGroups() {
		return excludedGroups;
	}

	public String getExcludedArtifacts() {
		return excludedArtifacts;
	}
}
