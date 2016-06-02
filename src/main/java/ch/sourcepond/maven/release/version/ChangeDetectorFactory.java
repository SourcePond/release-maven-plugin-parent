package ch.sourcepond.maven.release.version;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.plugin.logging.Log;

import ch.sourcepond.maven.release.scm.SCMRepository;

@Named
@Singleton
class ChangeDetectorFactory {
	private final Log log;
	private final SCMRepository repository;

	@Inject
	ChangeDetectorFactory(final Log pLog, final SCMRepository pRepository) {
		log = pLog;
		repository = pRepository;
	}

	ChangeDetector newDetector() {
		return new ChangeDetector(log, repository);
	}
}
