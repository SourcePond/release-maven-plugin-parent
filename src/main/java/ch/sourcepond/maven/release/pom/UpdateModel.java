package ch.sourcepond.maven.release.pom;

import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;

import ch.sourcepond.maven.release.reactor.UnresolvedSnapshotDependencyException;

@Component(role = Command.class, hint = "UpdateModel")
final class UpdateModel extends Command {
	static final String ERROR_FORMAT = "Project not found in reactor: %s";

	@Override
	public void alterModel(final Context updateContext) {
		final MavenProject project = updateContext.getProject();
		final Model model = updateContext.getModel();
		try {
			model.setVersion(updateContext.getVersionToDependOn(project.getGroupId(), project.getArtifactId()));
		} catch (final UnresolvedSnapshotDependencyException e) {
			updateContext.addError(ERROR_FORMAT, project);
		}
	}
}