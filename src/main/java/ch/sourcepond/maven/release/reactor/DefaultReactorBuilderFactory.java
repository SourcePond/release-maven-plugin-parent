package ch.sourcepond.maven.release.reactor;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.plugin.logging.Log;

import ch.sourcepond.maven.release.version.VersionBuilderFactory;

/**
 * @author rolandhauser
 *
 */
@Named
@Singleton
final class DefaultReactorBuilderFactory implements ReactorBuilderFactory {
	private final Log log;
	private final VersionBuilderFactory versionFactory;

	@Inject
	DefaultReactorBuilderFactory(final Log pLog, final VersionBuilderFactory pVersionFactory) {
		log = pLog;
		versionFactory = pVersionFactory;
	}

	@Override
	public ReactorBuilder newBuilder() {
		return new DefaultReactorBuilder(log, versionFactory);
	}

}
