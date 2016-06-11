package ch.sourcepond.maven.release.providers;

import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import ch.sourcepond.maven.release.PluginException;
import ch.sourcepond.maven.release.reactor.ReactorProjects;

public interface MavenComponentSingletons {

	Log getLog();

	MavenProject getProject();

	ReactorProjects getReactorProjects();

	void initialize(Log pLog, MavenProject pProject, List<MavenProject> pReactorProjects) throws PluginException;
}
