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
package ch.sourcepond.maven.release.commons;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;

import java.util.LinkedList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

@SuppressWarnings("serial")
public class PluginException extends Exception {
	protected final List<String> messages = new LinkedList<>();

	public PluginException(final Throwable cause, final String format, final Object... args) {
		super(format(format, args), cause);
	}

	public PluginException(final String format, final Object... args) {
		super(format(format, args));
	}

	public PluginException add(final String format, final Object... args) {
		messages.add(format(format, args));
		return this;
	}

	public List<String> getMessages() {
		return unmodifiableList(messages);
	}

	private void printCause(final Log log) {
		if (getCause() != null) {
			log.error(format("Caused by %s", getCause().getClass()));
			log.error(getCause().getMessage());
		}

		if (getCause() instanceof PluginException) {
			final PluginException plex = (PluginException) getCause();
			for (final String line : plex.messages) {
				log.error(line);
			}
			log.error("");
			plex.printCause(log);
		}
	}

	public void printBigErrorMessageAndThrow(final Log log) throws MojoExecutionException {
		log.error("");
		log.error("");
		log.error("");
		log.error("************************************");
		log.error("Could not execute the release plugin");
		log.error("************************************");
		log.error("");
		log.error("");
		log.error(getMessage());
		for (final String line : messages) {
			log.error(line);
		}
		log.error("");
		log.error("");
		printCause(log);
		throw new MojoExecutionException(getMessage());
	}

}
