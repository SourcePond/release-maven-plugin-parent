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

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;

/**
 * Resolves the actual version of an original object (like {@link Dependency} or
 * {@link Plugin} instances). This is necessary if the version of an artifact is
 * specified as property rather than directly declared in the appropriate a
 * &lt;dependency&gt; or &lt;plugin&gt; element. In this case, the
 * &lt;dependency&gt; or &lt;plugin&gt; element of the original model contains a
 * variable like $&#123;fooVersion&#125; which must be substituted before
 * further processing.
 *
 *
 */
public interface VersionSubstitution {

	/**
	 * Determines the actual version of the original {@link Dependency}
	 * specified.
	 * 
	 * @param project
	 *            Maven project; must not be {@code null}
	 * @param originalDependency
	 *            Original dependency, must not be {@code null}
	 * @return Actual version, never {@code null}
	 * @throws IllegalStateException
	 *             Thrown, if no substituted dependency could be found for the
	 *             dependency specified.
	 */
	String getActualVersionOrNull(MavenProject project, Dependency originalDependency);

	/**
	 * Determines the actual version of the original {@link Plugin} specified.
	 * 
	 * @param project
	 *            Maven project; must not be {@code null}
	 * @param originalPlugin
	 *            Original plugin, must not be {@code null}
	 * @return Actual version, never {@code null}
	 * @throws IllegalStateException
	 *             Thrown, if no substituted dependency could be found for the
	 *             dependency specified.
	 */
	String getActualVersionOrNull(MavenProject project, Plugin originalPlugin);
}