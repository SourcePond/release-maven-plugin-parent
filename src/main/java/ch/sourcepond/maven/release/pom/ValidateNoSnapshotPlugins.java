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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import ch.sourcepond.maven.release.substitution.VersionSubstitution;

/**
 * Validates that POM to be release does not refer a plugin with a snapshot
 * version. The only exception is the multi-module-release-maven-plugin itself
 * (necessary for testing).
 *
 */
@Named("ValidateNoSnapshotPlugins")
@Singleton
final class ValidateNoSnapshotPlugins extends Command {
	static final String ERROR_FORMAT = "%s references plugin %s %s";
	static final String MULTI_MODULE_MAVEN_PLUGIN_GROUP_ID = "ch.sourcepond.maven.plugins";
	static final String MULTI_MODULE_MAVEN_PLUGIN_ARTIFACT_ID = "multi-module-release-maven-plugin";
	private final VersionSubstitution substitution;

	@Inject
	ValidateNoSnapshotPlugins(final Log log, final VersionSubstitution pSubstitution) {
		super(log);
		substitution = pSubstitution;
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

	private static boolean isMultiModuleReleasePlugin(final Plugin plugin) {
		return MULTI_MODULE_MAVEN_PLUGIN_GROUP_ID.equals(plugin.getGroupId())
				&& MULTI_MODULE_MAVEN_PLUGIN_ARTIFACT_ID.equals(plugin.getArtifactId());
	}
}