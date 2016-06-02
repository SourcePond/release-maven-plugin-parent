package ch.sourcepond.maven.release.pom;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;

import ch.sourcepond.maven.release.reactor.Reactor;

@Named
@Singleton
class ContextFactory {

	Context newContext(final Reactor reactor, final MavenProject project, final Model model,
			final boolean incrementSnapshotVersionAfterRelease) {
		return new Context(reactor, project, model, incrementSnapshotVersionAfterRelease);
	}
}
