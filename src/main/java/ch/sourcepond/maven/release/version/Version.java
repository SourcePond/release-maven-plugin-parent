package ch.sourcepond.maven.release.version;

public interface Version {
	String SNAPSHOT_EXTENSION = "-SNAPSHOT";

	String getNextDevelopmentVersion();

	String getReleaseVersion();

	String getBusinessVersion();

	long getBuildNumber();

	String getEquivalentVersionOrNull();

	void makeReleaseable();

	boolean hasChanged();
}