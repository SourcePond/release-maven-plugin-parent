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
package ch.sourcepond.maven.release.commons;

import static java.lang.String.format;

public abstract class BaseVersion implements Version {
	static final String SNAPSHOT_FORMAT = "%s.%d-SNAPSHOT";

	@Override
	public final String getNextDevelopmentVersion() {
		return format(SNAPSHOT_FORMAT, getBusinessVersion(), getBuildNumber() + 1);
	}

	@Override
	public final String getDevelopmentVersion() {
		return format(SNAPSHOT_FORMAT, getBusinessVersion(), getBuildNumber());
	}

	@Override
	public String toString() {
		return format(
				"[build-number: %d, business-version: %s, release-version: %s, development-version: %s, next-development-version: %s, equivalent-version: %s]",
				getBuildNumber(), getBusinessVersion(), getReleaseVersion(), getDevelopmentVersion(),
				getNextDevelopmentVersion(), getEquivalentVersionOrNull());
	}
}