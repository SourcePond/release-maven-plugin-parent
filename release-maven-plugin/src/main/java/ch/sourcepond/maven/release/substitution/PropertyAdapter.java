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

/**
 * Adapts the properties from the original object.
 *
 *
 * @param <T>
 *            Type of the original object like {@link Dependency} or
 *            {@link Plugin}.
 */
interface PropertyAdapter<T> {

	/**
	 * Returns the {@code artifactId} of the original object.
	 * 
	 * @param origin
	 *            Original object to get the artifactId from; must not be
	 *            {@code null}
	 * @return Origin artifactId, never {@code null}
	 */
	String getArtifactId(T origin);

	/**
	 * Returns the {@code groupId} of the original object.
	 * 
	 * @param origin
	 *            Original object to get the groupId from; must not be
	 *            {@code null}
	 * @return Origin groupId, never {@code null}
	 */
	String getGroupId(T origin);

	/**
	 * Returns the {@code version} of the original object.
	 * 
	 * @param origin
	 *            Original object to get the version from; must not be
	 *            {@code null}
	 * @return Origin version, never {@code null}
	 */
	String getVersion(T origin);
}