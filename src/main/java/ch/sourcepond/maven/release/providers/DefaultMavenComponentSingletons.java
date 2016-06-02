package ch.sourcepond.maven.release.providers;

import java.util.Iterator;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import ch.sourcepond.maven.release.reactor.ReactorProjects;

@Named
@Singleton
final class DefaultMavenComponentSingletons implements MavenComponentSingletons {
	private Log log;
	private MavenProject project;
	private ReactorProjects reactorProjects;

	@Override
	public Log getLog() {
		return log;
	}

	@Override
	public MavenProject getProject() {
		return project;
	}

	@Override
	public ReactorProjects getReactorProjects() {
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
