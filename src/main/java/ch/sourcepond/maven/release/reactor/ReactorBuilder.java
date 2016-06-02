package ch.sourcepond.maven.release.reactor;

import java.util.List;

public interface ReactorBuilder {

	ReactorBuilder setUseLastDigitAsBuildNumber(boolean useLastDigitAsVersionNumber);

	ReactorBuilder setBuildNumber(final Long buildNumber);

	ReactorBuilder setModulesToForceRelease(final List<String> modulesToForceRelease);

	ReactorBuilder setRemoteUrl(String remoteUrl);

	Reactor build() throws ReactorException;
}
