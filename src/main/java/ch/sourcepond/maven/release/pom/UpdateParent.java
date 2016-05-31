package ch.sourcepond.maven.release.pom;

import static java.lang.String.format;

import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;

import ch.sourcepond.maven.release.reactor.UnresolvedSnapshotDependencyException;

/**
 * @author rolandhauser
 *
 */
@Component(role = Command.class, hint = "UpdateParent")
final class UpdateParent extends Command {
	static final String ERROR_FORMAT = "The parent of %s is %s %s";

	@Override
	public void alterModel(final Context updateContext) {
		final MavenProject project = updateContext.getProject();
		final Model model = updateContext.getModel();
		final MavenProject parent = project.getParent();

		if (parent != null && isSnapshot(parent.getVersion())) {
			try {
				final String versionToDependOn = updateContext.getVersionToDependOn(parent.getGroupId(),
						parent.getArtifactId());
				model.getParent().setVersion(versionToDependOn);
				log.debug(format(" Parent %s rewritten to version %s", parent.getArtifactId(), versionToDependOn));
			} catch (final UnresolvedSnapshotDependencyException e) {
				updateContext.addError(ERROR_FORMAT, project.getArtifactId(), e.artifactId, parent.getVersion());
			}
		}
	}
}