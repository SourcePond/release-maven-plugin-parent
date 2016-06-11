package ch.sourcepond.maven.release.providers;

import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import ch.sourcepond.maven.release.PluginException;
import ch.sourcepond.maven.release.reactor.ReactorProjects;

@Named
@Singleton
final class DefaultMavenComponentSingletons implements MavenComponentSingletons {
	private final List<Initializable> initializables;
	private Log log;
	private MavenProject project;
	private ReactorProjects reactorProjects;

	@Inject
	DefaultMavenComponentSingletons(final List<Initializable> pInitializables) {
		initializables = pInitializables;
	}

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

	private void setLog(final Log pLog) {
		log = pLog;
	}

	private void setRootProject(final MavenProject pProject) {
		project = pProject;
	}

	private void setReactorProjects(final List<MavenProject> pReactorProjects) {
		reactorProjects = new ReactorProjects() {

			@Override
			public Iterator<MavenProject> iterator() {
				return pReactorProjects.iterator();
			}
		};
	}

	@Override
	public void initialize(final Log pLog, final MavenProject pProject, final List<MavenProject> pReactorProjects)
			throws PluginException {
		setLog(pLog);
		setRootProject(pProject);
		setReactorProjects(pReactorProjects);

		if (initializables.isEmpty()) {
			throw new PluginException("No initializables found!");
		}

		for (final Initializable initializable : initializables) {
			initializable.initialize();
		}
	}
}
