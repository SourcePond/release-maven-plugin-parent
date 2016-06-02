package ch.sourcepond.maven.release.providers;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

@Named
@Singleton
final class DefaultMavenComponentSingletons implements MavenComponentSingletons {
	private Log log;
	private MavenProject project;

	Log getLog() {
		return log;
	}

	MavenProject getProject() {
		return project;
	}

	@Override
	public void setLog(final Log pLog) {
		log = pLog;
	}

	@Override
	public void setRootProject(final MavenProject pProject) {
		project = pProject;
	}
}
