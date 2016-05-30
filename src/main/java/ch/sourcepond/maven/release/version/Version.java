package ch.sourcepond.maven.release.version;

public interface Version {

	String getReleaseVersion();

	String getBusinessVersion();

	long getBuildNumber();

	String getEquivalentVersionOrNull();

	void makeReleaseable();
}
