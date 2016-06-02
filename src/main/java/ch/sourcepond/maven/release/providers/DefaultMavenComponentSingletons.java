package ch.sourcepond.maven.release.providers;

import java.util.Iterator;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

@Named
@Singleton
final class DefaultMavenComponentSingletons implements MavenComponentSingletons {
	private Log log;
	private MavenProject project;
	private ReactorProjects reactorProjects;

	Log getLog() {
		return log;
	}

	MavenProject getProject() {
		return project;
	}

	ReactorProjects getReactorProjects() {
		return reactorProjects;
	}

	@Override
	public void setLog(final Log pLog) {
		log = pLog;
	}

	@Override
	public void setRootProject(final MavenProject pProject) {
		project = pProject;
	}

	@Override
	public void setReactorProjects(final List<MavenProject> pReactorProjects) {
		reactorProjects = new ReactorProjects() {

			@Override
			public Iterator<MavenProject> iterator() {
				return pReactorProjects.iterator();
			}
		};
	}
}
