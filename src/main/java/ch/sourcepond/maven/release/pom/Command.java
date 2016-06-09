package ch.sourcepond.maven.release.pom;

import org.apache.maven.plugin.logging.Log;

/**
 * @author rolandhauser
 *
 */
abstract class Command implements Comparable<Command>{
	protected final Log log;

	Command(final Log pLog) {
		log = pLog;
	}

	static boolean isSnapshot(final String versionOrNull) {
		return versionOrNull != null && versionOrNull.endsWith("-SNAPSHOT");
	}

	public abstract void alterModel(Context updateContext);
	
	protected abstract Integer priority();
	
	@Override
	public int compareTo(Command o) {
		return priority().compareTo(o.priority());
	}

}