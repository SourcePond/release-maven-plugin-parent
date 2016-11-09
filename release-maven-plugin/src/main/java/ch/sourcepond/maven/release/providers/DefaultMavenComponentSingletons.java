/*Copyright (C) 2016 Roland Hauser, <sourcepond@gmail.com>
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package ch.sourcepond.maven.release.providers;

import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import ch.sourcepond.maven.release.commons.PluginException;
import ch.sourcepond.maven.release.config.Configuration;
import ch.sourcepond.maven.release.config.ConfigurationAccessor;
import ch.sourcepond.maven.release.reactor.ReactorProjects;

@Named
@Singleton
final class DefaultMavenComponentSingletons implements MavenComponentSingletons {
	private final List<Initializable> initializables;
	private Log log;
	private MavenProject project;
	private ReactorProjects reactorProjects;
	private Configuration configuration;

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

	@Override
	public Configuration getConfiguration() {
		return configuration;
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

	private void setConfiguration(final Configuration pConfiguration) {
		configuration = pConfiguration;
	}

	@Override
	public void initialize(final Mojo pMojo, final MavenProject pProject, final List<MavenProject> pReactorProjects)
			throws PluginException {
		setLog(pMojo.getLog());
		setRootProject(pProject);
		setReactorProjects(pReactorProjects);
		setConfiguration(new ConfigurationAccessor(pMojo));

		for (final Initializable initializable : initializables) {
			initializable.initialize();
		}
	}
}
