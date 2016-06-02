package ch.sourcepond.maven.release.pom;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import ch.sourcepond.maven.release.scm.SCMRepository;

@Component(role = ChangeSetCreatorFactory.class)
class ChangeSetCreatorFactory {

	@Requirement(role = SCMRepository.class)
	private SCMRepository repository;

	@Requirement(role = MavenXpp3WriterFactory.class)
	private MavenXpp3WriterFactory writerFactory;

	@Requirement(role = Log.class)
	private Log log;

	void setRepository(final SCMRepository repository) {
		this.repository = repository;
	}

	void setMavenXpp3WriterFactory(final MavenXpp3WriterFactory writerFactory) {
		this.writerFactory = writerFactory;
	}

	void setLog(final Log log) {
		this.log = log;
	}

	DefaultChangeSet newChangeSet(final String remoteUrlOrNull) {
		return new DefaultChangeSet(log, repository, writerFactory.newWriter(), remoteUrlOrNull);
	}
}
