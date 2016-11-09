/*Copyright (C) 2016 Roland Hauser, <sourcepond@gmail.com>
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package ch.sourcepond.maven.release.version;

import static java.lang.Long.valueOf;

import org.apache.maven.project.MavenProject;

import ch.sourcepond.maven.release.commons.Version;
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
		if (configuration.isIncrementSnapshotVersionAfterRelease()) {
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
		version.setEquivalentVersion(detectorFactory.newDetector().setProject(project)
				.setActualBuildNumber(actualBuildNumber).setChangedDependency(changedDependencyOrNull)
				.setRelativePathToModule(relativePathToModuleOrNull).setBusinessVersion(businessVersion)
				.equivalentVersionOrNull());
		return version;
	}

}