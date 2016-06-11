package ch.sourcepond.maven.release.reactor;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import ch.sourcepond.maven.release.providers.RootProject;
import ch.sourcepond.maven.release.version.VersionBuilder;
import ch.sourcepond.maven.release.version.VersionBuilderFactory;
import ch.sourcepond.maven.release.version.VersionException;

@Named
@Singleton
final class DefaultReactorFactory implements ReactorFactory {
	private final Log log;
	private final VersionBuilderFactory versionBuilderFactory;
	private final RootProject rootProject;
	private final ReactorProjects projects;
	private List<String> modulesToForceRelease;

	@Inject
	DefaultReactorFactory(final Log log, final VersionBuilderFactory versioningFactory, final RootProject pRootProject,
			final ReactorProjects pProjects) {
		this.log = log;
		this.versionBuilderFactory = versioningFactory;
		rootProject = pRootProject;
		projects = pProjects;
	}

	@Override
	public ReactorFactory setModulesToForceRelease(final List<String> modulesToForceRelease) {
		this.modulesToForceRelease = modulesToForceRelease;
		return this;
	}

	@Override
	public Reactor newReactor() throws ReactorException {
		final DefaultReactor reactor = new DefaultReactor(log);

		for (final MavenProject project : projects) {
			try {
				final String relativePathToModule = rootProject.calculateModulePath(project);
				final String changedDependencyOrNull = reactor.getChangedDependencyOrNull(project);
				final VersionBuilder versionBuilder = versionBuilderFactory.newBuilder();
				versionBuilder.setProject(project);
				versionBuilder.setChangedDependency(changedDependencyOrNull);

				if (modulesToForceRelease == null || !modulesToForceRelease.contains(project.getArtifactId())) {
					versionBuilder.setRelativePath(relativePathToModule);
				}

				reactor.addReleasableModule(
						new ReleasableModule(project, versionBuilder.build(), relativePathToModule));
			} catch (final VersionException e) {
				throw new ReactorException(e, "Version could be created for project %s", project);
			}
		}

		return reactor.finalizeReleaseVersions();
	}
}
