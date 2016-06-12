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

import org.apache.maven.plugin.logging.Log;

/**
 * @author rolandhauser
 *
 */
abstract class Command implements Comparable<Command> {
	protected final Log log;

	Command(final Log pLog) {
		log = pLog;
	}

	static boolean isSnapshot(final String versionOrNull) {
		return versionOrNull != null && versionOrNull.endsWith("-SNAPSHOT");
	}

	public abstract void alterModel(Context updateContext);

	protected abstract Integer priority();

	@Override
	public int compareTo(final Command o) {
		return priority().compareTo(o.priority());
	}

}