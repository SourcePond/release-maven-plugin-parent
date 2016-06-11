package ch.sourcepond.maven.release.reactor;

public interface ReactorFactory {

	Reactor newReactor() throws ReactorException;
}
