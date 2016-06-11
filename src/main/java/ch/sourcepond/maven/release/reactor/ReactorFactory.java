package ch.sourcepond.maven.release.reactor;

import java.util.List;

public interface ReactorFactory {

	ReactorFactory setUseLastNumber(boolean useLastDigitAsVersionNumber);

	ReactorFactory setModulesToForceRelease(final List<String> modulesToForceRelease);

	Reactor newReactor() throws ReactorException;
}
