package ch.sourcepond.maven.release.providers;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.plugin.logging.Log;

@Named
@Singleton
final class DefaultMavenComponentSingletons implements MavenComponentSingletons {
	private Log log;

	Log getLog() {
		return log;
	}

	@Override
	public void setLog(final Log pLog) {
		log = pLog;
	}
}
