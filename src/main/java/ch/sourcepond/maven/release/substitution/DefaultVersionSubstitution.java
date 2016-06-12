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
package ch.sourcepond.maven.release.substitution;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;

/**
 * Default implementation of the {@link VersionSubstitution} interface.
 *
 */
@Named
@Singleton
class DefaultVersionSubstitution implements VersionSubstitution {
	private final PropertyAdapter<Dependency> dependencyAdapter;
	private final PropertyAdapter<Plugin> pluginAdapter;

	@Inject
	DefaultVersionSubstitution(final PropertyAdapter<Dependency> pDependencyAdapter,
			final PropertyAdapter<Plugin> pPluginAdapter) {
		dependencyAdapter = pDependencyAdapter;
		pluginAdapter = pPluginAdapter;
	}

	/**
	 * Converts a list of original objects (like {@link Dependency} or
	 * {@link Plugin} objects) to a new list of appropriate {@link Artifact}
	 * instances.
	 * 
	 * @param origins
	 *            List of original objects, must not be {@code null}
	 * @param adapter
	 *            Adapter to convert an original object into an {@link Artifact}
	 *            instance; must not be {@code null}.
	 * @return
	 */
	private <T> List<Artifact> convert(final List<T> origins, final PropertyAdapter<T> adapter) {
		final List<Artifact> artifacts = new LinkedList<>();
		for (final T origin : origins) {
			artifacts.add(new Artifact() {

				@Override
				public String getArtifactId() {
					return adapter.getArtifactId(origin);
				}

				@Override
				public String getGroupId() {
					return adapter.getGroupId(origin);
				}

				@Override
				public String getVersion() {
					return adapter.getVersion(origin);
				}

			});
		}

		return artifacts;
	}

	/**
	 * @param substituted
	 * @param origin
	 * @param adapter
	 * @return
	 */
	private <T> String getActualVersionOrNull(final List<T> substituted, final T origin,
			final PropertyAdapter<T> adapter) {
		for (final Artifact artifact : convert(substituted, adapter)) {
			if (adapter.getGroupId(origin).equals(artifact.getGroupId())
					&& adapter.getArtifactId(origin).equals(artifact.getArtifactId())) {
				return artifact.getVersion();
			}
		}
		return null;
	}

	@Override
	public String getActualVersionOrNull(final MavenProject project, final Dependency originalDependency) {
		return getActualVersionOrNull(project.getDependencies(), originalDependency, dependencyAdapter);
	}

	@Override
	public String getActualVersionOrNull(final MavenProject project, final Plugin originalPlugin) {
		return getActualVersionOrNull(project.getBuildPlugins(), originalPlugin, pluginAdapter);
	}
}