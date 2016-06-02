package ch.sourcepond.maven.release.reactor;

import java.util.List;

import org.apache.maven.project.MavenProject;

public interface ReactorBuilder {

	ReactorBuilder setProjects(List<MavenProject> projects);

	ReactorBuilder setUseLastDigitAsBuildNumber(boolean useLastDigitAsVersionNumber);

	ReactorBuilder setBuildNumber(final Long buildNumber);

	ReactorBuilder setModulesToForceRelease(final List<String> modulesToForceRelease);

	ReactorBuilder setRemoteUrl(String remoteUrl);

	Reactor build() throws ReactorException;
}
