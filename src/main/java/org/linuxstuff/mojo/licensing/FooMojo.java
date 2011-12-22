package org.linuxstuff.mojo.licensing;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @goal foo
 * 
 */
public class FooMojo extends AbstractLicensingMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		readLicensingRequirements();

	}

}
