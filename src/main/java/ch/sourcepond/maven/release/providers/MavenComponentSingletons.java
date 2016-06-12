package ch.sourcepond.maven.release.providers;

import java.util.List;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import ch.sourcepond.maven.release.PluginException;
import ch.sourcepond.maven.release.config.Configuration;
import ch.sourcepond.maven.release.reactor.ReactorProjects;

public interface MavenComponentSingletons {

	Log getLog();

	MavenProject getProject();

	ReactorProjects getReactorProjects();

	Configuration getConfiguration();

	void initialize(Mojo pMojo, MavenProject pProject, List<MavenProject> pReactorProjects) throws PluginException;

}
