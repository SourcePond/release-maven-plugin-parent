package ch.sourcepond.maven.release.pom;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.model.Model;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import ch.sourcepond.maven.release.reactor.UnresolvedSnapshotDependencyException;

@Named("UpdateModel")
@Singleton
final class UpdateModel extends Command {
	static final String ERROR_FORMAT = "Project not found in reactor: %s";

	@Inject
	UpdateModel(final Log pLog) {
		super(pLog);
	}

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