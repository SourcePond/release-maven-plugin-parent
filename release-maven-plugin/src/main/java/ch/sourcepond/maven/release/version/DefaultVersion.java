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
package ch.sourcepond.maven.release.version;

import ch.sourcepond.maven.release.commons.BaseVersion;
import ch.sourcepond.maven.release.commons.Version;

final class DefaultVersion extends BaseVersion implements Version {
	private String equivalentVersion;
	private String businessVersion;
	private String releaseVersion;
	private long buildNumber;

	void setEquivalentVersion(final String equivalentVersion) {
		this.equivalentVersion = equivalentVersion;
	}

	void setBusinessVersion(final String businessVersion) {
		this.businessVersion = businessVersion;
	}

	void setReleaseVersion(final String releaseVersion) {
		this.releaseVersion = releaseVersion;
	}

	void setBuildNumber(final long buildNumber) {
		this.buildNumber = buildNumber;
	}

	/**
	 * For example, "1.0" if the development version is "1.0-SNAPSHOT"
	 */
	@Override
	public String getBusinessVersion() {
		return businessVersion;
	}

	@Override
	public long getBuildNumber() {
		return buildNumber;
	}

	/**
	 * The business version with the build number appended, e.g. "1.0.1"
	 */
	@Override
	public String getReleaseVersion() {
		return releaseVersion;
	}

	@Override
	public String getEquivalentVersionOrNull() {
		return equivalentVersion;
	}

	@Override
	public void makeReleaseable() {
		setEquivalentVersion(null);
	}

	@Override
	public boolean hasChanged() {
		return getEquivalentVersionOrNull() == null;
	}
}