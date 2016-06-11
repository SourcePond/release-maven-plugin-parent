package ch.sourcepond.maven.release.version;

import org.apache.maven.project.MavenProject;

public interface VersionBuilder {

	VersionBuilder setProject(MavenProject project);

	VersionBuilder setUseLastNumber(boolean useLastNumber);

	VersionBuilder setBuildNumber(Long buildNumberOrNull);

	VersionBuilder setRelativePath(String relativePathToModuleOrNull);

	VersionBuilder setChangedDependency(String changedDependencyOrNull);

	Version build() throws VersionException;
}
