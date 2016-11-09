/*Copyright (C) 2016 Roland Hauser, <sourcepond@gmail.com>
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package ch.sourcepond.maven.release.scm;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import ch.sourcepond.maven.release.commons.PluginException;

/**
 *
 */
@SuppressWarnings("serial")
public class SCMException extends PluginException {

	public SCMException(final Throwable cause, final String format, final Object... args) {
		super(cause, format, args);
	}

	public SCMException(final String format, final Object... args) {
		super(format, args);
	}

	@Override
	public SCMException add(final String format, final Object... args) {
		super.add(format, args);
		return this;
	}

	@Override
	public void printBigErrorMessageAndThrow(final Log log) throws MojoExecutionException {
		final StringWriter sw = new StringWriter();
		printStackTrace(new PrintWriter(sw));
		final String exceptionAsString = sw.toString();
		add("Could not release due to a Git error");
		add("There was an error while accessing the Git repository. The error returned from git was:");
		add("Stack trace:");
		add(exceptionAsString);
		super.printBigErrorMessageAndThrow(log);
	}

}
