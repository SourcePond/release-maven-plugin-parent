package ch.sourcepond.maven.release.providers;

import java.util.Collection;

import org.apache.maven.project.MavenProject;

import ch.sourcepond.maven.release.reactor.ReactorException;

public interface RootProject {

	Collection<String> getActiveProfileIds();

	String calculateModulePath(MavenProject project) throws ReactorException;
}
