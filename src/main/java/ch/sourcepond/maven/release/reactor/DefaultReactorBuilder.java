package ch.sourcepond.maven.release.reactor;

import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import ch.sourcepond.maven.release.providers.RootProject;
import ch.sourcepond.maven.release.version.VersionBuilder;
import ch.sourcepond.maven.release.version.VersionBuilderFactory;
import ch.sourcepond.maven.release.version.VersionException;

final class DefaultReactorBuilder implements ReactorBuilder {
	private final Log log;
	private final VersionBuilderFactory versionBuilderFactory;
	private final RootProject rootProject;
	private List<MavenProject> projects;
	private boolean useLastDigitAsBuildNumber;
	private Long buildNumber;
	private List<String> modulesToForceRelease;
	private String remoteUrl;

	public DefaultReactorBuilder(final Log log, final VersionBuilderFactory versioningFactory,
			final RootProject pRootProject) {
		this.log = log;
		this.versionBuilderFactory = versioningFactory;
		rootProject = pRootProject;
	}

	@Override
	public ReactorBuilder setProjects(final List<MavenProject> projects) {
		this.projects = projects;
		return this;
	}

	@Override
	public ReactorBuilder setBuildNumber(final Long buildNumber) {
		this.buildNumber = buildNumber;
		return this;
	}

	@Override
	public ReactorBuilder setUseLastDigitAsBuildNumber(final boolean useLastDigitAsBuildNumber) {
		this.useLastDigitAsBuildNumber = useLastDigitAsBuildNumber;
		return this;
	}

	@Override
	public ReactorBuilder setModulesToForceRelease(final List<String> modulesToForceRelease) {
		this.modulesToForceRelease = modulesToForceRelease;
		return this;
	}

	@Override
	public Reactor build() throws ReactorException {
		final DefaultReactor reactor = new DefaultReactor(log);

		for (final MavenProject project : projects) {
			try {
				final String relativePathToModule = rootProject.calculateModulePath(project);
				final String changedDependencyOrNull = reactor.getChangedDependencyOrNull(project);
				final VersionBuilder versionBuilder = versionBuilderFactory.newBuilder();
				versionBuilder.setProject(project);
				versionBuilder.setUseLastDigitAsBuildNumber(useLastDigitAsBuildNumber);
				versionBuilder.setBuildNumber(buildNumber);
				versionBuilder.setChangedDependency(changedDependencyOrNull);
				versionBuilder.setRemoteUrl(remoteUrl);

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

	@Override
	public ReactorBuilder setRemoteUrl(final String remoteUrl) {
		this.remoteUrl = remoteUrl;
		return this;
	}
}
