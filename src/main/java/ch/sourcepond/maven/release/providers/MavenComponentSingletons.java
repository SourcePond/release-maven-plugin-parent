package ch.sourcepond.maven.release.providers;

import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

public interface MavenComponentSingletons {

	void setLog(Log pLog);

	void setRootProject(MavenProject pProject);

	void setReactorProjects(List<MavenProject> pReactorProjects);
}
