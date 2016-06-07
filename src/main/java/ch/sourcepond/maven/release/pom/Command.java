package ch.sourcepond.maven.release.pom;

import org.apache.maven.plugin.logging.Log;

/**
 * @author rolandhauser
 *
 */
abstract class Command {
	protected final Log log;

	Command(final Log pLog) {
		log = pLog;
	}

	static boolean isSnapshot(final String version) {
		return version != null && version.endsWith("-SNAPSHOT");
	}

	public abstract void alterModel(Context updateContext);
}
