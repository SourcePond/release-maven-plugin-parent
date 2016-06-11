package ch.sourcepond.maven.release.pom;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.logging.Log;

import ch.sourcepond.maven.release.providers.RootProject;
import ch.sourcepond.maven.release.scm.SCMRepository;

@Named
@Singleton
class DefaultChangeSetFactory {
	private final Log log;
	private final SCMRepository repository;
	private final MavenXpp3Writer writer;
	private final RootProject rootProject;

	@Inject
	DefaultChangeSetFactory(final Log pLog, final SCMRepository pRepository, final MavenXpp3Writer pWriter,
			final RootProject pRootProject) {
		log = pLog;
		repository = pRepository;
		writer = pWriter;
		rootProject = pRootProject;
	}

	DefaultChangeSet newChangeSet() {
		return new DefaultChangeSet(log, repository, writer, rootProject);
	}
}
