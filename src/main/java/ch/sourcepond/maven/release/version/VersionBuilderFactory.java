package ch.sourcepond.maven.release.version;

/**
 * Factory interface for creating new {@link Version} instances.
 */
public interface VersionBuilderFactory {

	VersionBuilder newBuilder();

}
