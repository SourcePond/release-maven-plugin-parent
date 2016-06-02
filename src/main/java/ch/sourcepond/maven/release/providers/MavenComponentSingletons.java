package ch.sourcepond.maven.release.providers;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

public interface MavenComponentSingletons {

	void setLog(Log pLog);

	void setRootProject(MavenProject pProject);
}
