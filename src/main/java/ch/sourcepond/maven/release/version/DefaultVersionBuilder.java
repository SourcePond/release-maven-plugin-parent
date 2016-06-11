package ch.sourcepond.maven.release.version;

import static java.lang.Long.valueOf;

import org.apache.maven.project.MavenProject;

import ch.sourcepond.maven.release.config.Configuration;

/**
 *
 */
final class DefaultVersionBuilder implements VersionBuilder {
	static final String SNAPSHOT_EXTENSION = "-SNAPSHOT";
	private final BuildNumberFinder finder;
	private final Configuration configuration;
	private final ChangeDetectorFactory detectorFactory;
	private MavenProject project;
	private boolean userLastNumber;
	private String relativePathToModuleOrNull;
	private String changedDependencyOrNull;

	DefaultVersionBuilder(final BuildNumberFinder pFinder, final Configuration pConfiguration,
			final ChangeDetectorFactory pDetectorFactory) {
		finder = pFinder;
		configuration = pConfiguration;
		detectorFactory = pDetectorFactory;
	}

	@Override
	public VersionBuilder setProject(final MavenProject project) {
		this.project = project;
		return this;
	}

	@Override
	public VersionBuilder setUseLastNumber(final boolean useLastNumber) {
		this.userLastNumber = useLastNumber;
		return this;
	}

	@Override
	public VersionBuilder setRelativePath(final String relativePathToModuleOrNull) {
		this.relativePathToModuleOrNull = relativePathToModuleOrNull;
		return this;
	}

	@Override
	public VersionBuilder setChangedDependency(final String changedDependencyOrNull) {
		this.changedDependencyOrNull = changedDependencyOrNull;
		return this;
	}

	@Override
	public Version build() throws VersionException {
		String businessVersion = project.getVersion().replace(SNAPSHOT_EXTENSION, "");
		long actualBuildNumber;

		final Long buildNumberOrNull = configuration.getBuildNumberOrNull();
		if (userLastNumber) {
			final int idx = businessVersion.lastIndexOf('.');
			actualBuildNumber = valueOf(businessVersion.substring(idx + 1));
			businessVersion = businessVersion.substring(0, idx);

			if (buildNumberOrNull != null && buildNumberOrNull > actualBuildNumber) {
				actualBuildNumber = buildNumberOrNull;
			}
		} else if (buildNumberOrNull != null) {
			actualBuildNumber = buildNumberOrNull;
		} else {
			actualBuildNumber = finder.findBuildNumber(project, businessVersion);
		}

		final String releaseVersion = businessVersion + "." + actualBuildNumber;
		final DefaultVersion version = new DefaultVersion();
		version.setReleaseVersion(releaseVersion);
		version.setBuildNumber(actualBuildNumber);
		version.setBusinessVersion(businessVersion);
		version.setEquivalentVersion(detectorFactory.newDetector().setProject(project).setActualBuildNumber(actualBuildNumber)
				.setChangedDependency(changedDependencyOrNull).setRelativePathToModule(relativePathToModuleOrNull)
				.setBusinessVersion(businessVersion).equivalentVersionOrNull());
		return version;
	}

}