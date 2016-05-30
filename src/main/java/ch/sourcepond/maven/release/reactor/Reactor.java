package ch.sourcepond.maven.release.reactor;

public interface Reactor extends Iterable<ReleasableModule> {

	ReleasableModule findByLabel(String label);

	ReleasableModule find(String groupId, String artifactId) throws UnresolvedSnapshotDependencyException;
}
