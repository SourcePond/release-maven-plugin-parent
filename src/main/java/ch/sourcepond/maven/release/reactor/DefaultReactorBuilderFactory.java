package ch.sourcepond.maven.release.reactor;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.plugin.logging.Log;

import ch.sourcepond.maven.release.providers.ReactorProjects;
import ch.sourcepond.maven.release.providers.RootProject;
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
	private final RootProject rootProject;
	private final ReactorProjects projects;

	@Inject
	DefaultReactorBuilderFactory(final Log pLog, final VersionBuilderFactory pVersionFactory,
			final RootProject pRootProject, final ReactorProjects pProjects) {
		log = pLog;
		versionFactory = pVersionFactory;
		rootProject = pRootProject;
		projects = pProjects;
	}

	@Override
	public ReactorBuilder newBuilder() {
		return new DefaultReactorBuilder(log, versionFactory, rootProject, projects);
	}

}
