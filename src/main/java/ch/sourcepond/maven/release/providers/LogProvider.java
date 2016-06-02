package ch.sourcepond.maven.release.providers;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.plugin.logging.Log;

/**
 *
 */
@Named
@Singleton
final class LogProvider extends BaseProvider<Log> {

	@Inject
	LogProvider(final DefaultMavenComponentSingletons pSingletons) {
		super(pSingletons);
	}

	@Override
	protected Log getDelegate() {
		return singletons.getLog();
	}

	@Override
	protected Class<Log> getDelegateType() {
		return Log.class;
	}

}
