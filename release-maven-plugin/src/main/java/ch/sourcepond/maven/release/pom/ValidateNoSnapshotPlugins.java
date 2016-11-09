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
package ch.sourcepond.maven.release.pom;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import ch.sourcepond.maven.release.substitution.VersionSubstitution;

/**
 * Validates that POM to be release does not refer a plugin with a snapshot
 * version. The only exception is the release-maven-plugin itself (necessary for
 * testing).
 *
 */
@Named("ValidateNoSnapshotPlugins")
@Singleton
final class ValidateNoSnapshotPlugins extends Command {
	static final String PROC_PLUGIN_EXCLUDE = "/proc_plugin_exclude";
	static final String ERROR_FORMAT = "%s references plugin %s %s";
	private final VersionSubstitution substitution;
	private final String releasePluginGroupId;
	private final String releasePluginArtifactId;

	@Inject
	ValidateNoSnapshotPlugins(final Log log, final VersionSubstitution pSubstitution)
			throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		super(log);
		substitution = pSubstitution;

		try (final InputStream in = getClass().getResourceAsStream(PROC_PLUGIN_EXCLUDE)) {
			final Properties props = new Properties();
			props.load(in);
			releasePluginGroupId = props.getProperty("groupId");
			releasePluginArtifactId = props.getProperty("artifactId");
		}
	}

	@Override
	public void alterModel(final Context updateContext) {
		final MavenProject project = updateContext.getProject();
		for (final Plugin plugin : project.getModel().getBuild().getPlugins()) {
			final String substitutedVersionOrNull = substitution.getActualVersionOrNull(project, plugin);
			if (isSnapshot(substitutedVersionOrNull) && !isMultiModuleReleasePlugin(plugin)) {
				updateContext.addError(ERROR_FORMAT, project.getArtifactId(), plugin.getArtifactId(),
						substitutedVersionOrNull);
			}
		}
	}

	@Override
	protected Integer priority() {
		return 4;
	}

	private boolean isMultiModuleReleasePlugin(final Plugin plugin) {
		return releasePluginGroupId.equals(plugin.getGroupId())
				&& releasePluginArtifactId.equals(plugin.getArtifactId());
	}
}