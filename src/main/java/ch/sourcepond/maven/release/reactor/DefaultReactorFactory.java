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
package ch.sourcepond.maven.release.reactor;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import ch.sourcepond.maven.release.config.Configuration;
import ch.sourcepond.maven.release.providers.RootProject;
import ch.sourcepond.maven.release.version.VersionBuilder;
import ch.sourcepond.maven.release.version.VersionBuilderFactory;
import ch.sourcepond.maven.release.version.VersionException;

@Named
@Singleton
final class DefaultReactorFactory implements ReactorFactory {
	private final Log log;
	private final VersionBuilderFactory versionBuilderFactory;
	private final Configuration configuration;
	private final RootProject rootProject;
	private final ReactorProjects projects;

	@Inject
	DefaultReactorFactory(final Log pLog, final VersionBuilderFactory pVersioningFactory,
			final Configuration pConfiguration, final RootProject pRootProject, final ReactorProjects pProjects) {
		log = pLog;
		versionBuilderFactory = pVersioningFactory;
		configuration = pConfiguration;
		rootProject = pRootProject;
		projects = pProjects;
	}

	@Override
	public Reactor newReactor() throws ReactorException {
		final DefaultReactor reactor = new DefaultReactor(log);

		final List<String> modulesToForceRelease = configuration.getModulesToForceRelease();
		for (final MavenProject project : projects) {
			try {
				final String relativePathToModule = rootProject.calculateModulePath(project);
				final String changedDependencyOrNull = reactor.getChangedDependencyOrNull(project);
				final VersionBuilder versionBuilder = versionBuilderFactory.newBuilder();
				versionBuilder.setProject(project);
				versionBuilder.setChangedDependency(changedDependencyOrNull);

				if (!modulesToForceRelease.contains(project.getArtifactId())) {
					versionBuilder.setRelativePath(relativePathToModule);
				}

				reactor.addReleasableModule(
						new ReleasableModule(project, versionBuilder.build(), relativePathToModule));
			} catch (final VersionException e) {
				throw new ReactorException(e, "Version could be created for project %s", project);
			}
		}

		return reactor.finalizeReleaseVersions();
	}
}
