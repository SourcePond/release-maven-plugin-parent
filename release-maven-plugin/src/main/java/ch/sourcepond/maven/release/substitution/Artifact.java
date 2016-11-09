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
 * A view on an original object like {@link Dependency} or {@link Plugin} which
 * provides a {@code groupId}, {@code artifactId} and {@code version}.
 *
 */
interface Artifact {

	/**
	 * Returns the {@code artifactId} of the original object.
	 * 
	 * @return artifactId, never {@code null}
	 */
	String getArtifactId();

	/**
	 * Returns the {@code groupId} of the original object.
	 * 
	 * @return groupId, never {@code null}
	 */
	String getGroupId();

	/**
	 * Returns the actual version {@code groupId} of the original object.
	 * 
	 * @return groupId, never {@code null}
	 */
	String getVersion();
}