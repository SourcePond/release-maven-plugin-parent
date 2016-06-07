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
			final String version = substitution.getActualVersion(project, plugin);
			if (isSnapshot(version) && !isMultiModuleReleasePlugin(plugin)) {
				updateContext.addError(ERROR_FORMAT, project.getArtifactId(), plugin.getArtifactId(), version);
			}
		}
	}

	private static boolean isMultiModuleReleasePlugin(final Plugin plugin) {
		return MULTI_MODULE_MAVEN_PLUGIN_GROUP_ID.equals(plugin.getGroupId())
				&& MULTI_MODULE_MAVEN_PLUGIN_ARTIFACT_ID.equals(plugin.getArtifactId());
	}
}
