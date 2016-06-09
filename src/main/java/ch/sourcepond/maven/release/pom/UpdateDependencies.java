package ch.sourcepond.maven.release.pom;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import ch.sourcepond.maven.release.reactor.UnresolvedSnapshotDependencyException;
import ch.sourcepond.maven.release.substitution.VersionSubstitution;

/**
 * @author rolandhauser
 *
 */
@Named("UpdateDependencies")
@Singleton
class UpdateDependencies extends Command {
	static final String ERROR_FORMAT = "%s references dependency %s %s";
	private final VersionSubstitution substitution;

	@Inject
	UpdateDependencies(final Log log, final VersionSubstitution pSubstitution) {
		super(log);
		substitution = pSubstitution;
	}

	@Override
	public final void alterModel(final Context updateContext) {
		final MavenProject project = updateContext.getProject();
		final Model model = updateContext.getModel();

		for (final Dependency dependency : determineDependencies(model)) {
			if (isNotBlank(dependency.getVersion())) {
				final String substitutedVersionOrNull = substitution.getActualVersionOrNull(project, dependency);
				if (isSnapshot(substitutedVersionOrNull)) {
					try {
						final String versionToDependOn = updateContext.getVersionToDependOn(dependency.getGroupId(),
								dependency.getArtifactId());
						dependency.setVersion(versionToDependOn);
						log.debug(format(" Dependency on %s rewritten to version %s", dependency.getArtifactId(),
								versionToDependOn));
					} catch (final UnresolvedSnapshotDependencyException e) {
						updateContext.addError(ERROR_FORMAT, project.getArtifactId(), e.artifactId,
								substitutedVersionOrNull);
					}
				} else {
					log.debug(format(" Dependency on %s kept at version %s", dependency.getArtifactId(),
							substitutedVersionOrNull));
				}
			}
		}
	}

	protected List<Dependency> determineDependencies(final Model originalModel) {
		return originalModel.getDependencies();
	}

	@Override
	protected Integer priority() {
		return 1;
	}
}