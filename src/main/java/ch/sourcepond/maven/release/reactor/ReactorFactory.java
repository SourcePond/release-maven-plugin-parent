package ch.sourcepond.maven.release.reactor;

import java.util.List;

public interface ReactorFactory {

	ReactorFactory setUseLastDigitAsBuildNumber(boolean useLastDigitAsVersionNumber);

	ReactorFactory setBuildNumber(final Long buildNumber);

	ReactorFactory setModulesToForceRelease(final List<String> modulesToForceRelease);

	ReactorFactory setRemoteUrl(String remoteUrl);

	Reactor newReactor() throws ReactorException;
}
