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
package ch.sourcepond.maven.release.pom;

import ch.sourcepond.maven.release.reactor.Reactor;

public interface Updater {

	/**
	 * Updates all necessary POMs and returns the changed files.
	 * 
	 * @param reactor
	 *            Reactor instance, must not be {@code null}
	 * @param incrementSnapshotVersionAfterRelease
	 *            If {@code true}, the all module SNAPSHOT references will be
	 *            upgraded to the next SNAPSHOT-version
	 * @return List of updated POM files.
	 * @throws POMUpdateException
	 *             Thrown, if something went wrong during POM update
	 */
	ChangeSet updatePoms(Reactor reactor, boolean incrementSnapshotVersionAfterRelease) throws POMUpdateException;

}
