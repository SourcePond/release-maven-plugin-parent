package ch.sourcepond.maven.release.version;

import static java.lang.String.format;

public abstract class BaseVersion implements Version {
	static final String SNAPSHOT_EXTENSION = "-SNAPSHOT";

	@Override
	public String getNextDevelopmentVersion() {
		return getBusinessVersion() + "." + (getBuildNumber() + 1) + SNAPSHOT_EXTENSION;
	}

	@Override
	public String toString() {
		return format(
				"[build-number: %d, business-version: %s, release-version: %s, next-development-version: %s, equivalent-version: %s]",
				getBuildNumber(), getBusinessVersion(), getReleaseVersion(), getNextDevelopmentVersion(),
				getEquivalentVersionOrNull());
	}
}