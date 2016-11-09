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

import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.project.MavenProject;

import ch.sourcepond.maven.release.scm.ProposedTag;
import ch.sourcepond.maven.release.scm.SCMException;
import ch.sourcepond.maven.release.scm.SCMRepository;

@Named
@Singleton
class BuildNumberFinder {
	static final String SNAPSHOT_EXTENSION = "-SNAPSHOT";
	private final SCMRepository repository;

	@Inject
	BuildNumberFinder(final SCMRepository pRepository) {
		repository = pRepository;
	}

	public long findBuildNumber(final MavenProject project, final String businessVersion) throws VersionException {
		final SortedSet<Long> prev = new TreeSet<>();

		try {
			for (final ProposedTag previousTag : repository.tagsForVersion(project.getArtifactId(), businessVersion)) {
				prev.add(previousTag.getBuildNumber());
			}

			prev.addAll(repository.getRemoteBuildNumbers(project.getArtifactId(), businessVersion));
			return prev.isEmpty() ? 0l : prev.last() + 1;
		} catch (final SCMException e) {
			throw new VersionException(e, "Build number could not be determined!");
		}
	}

	public String newBusinessVersion(final MavenProject project, final boolean useLastDigitAsVersionNumber) {
		String businessVersion = project.getVersion().replace(SNAPSHOT_EXTENSION, "");
		if (useLastDigitAsVersionNumber) {
			businessVersion = businessVersion.substring(0, businessVersion.lastIndexOf('.'));
		}
		return businessVersion;
	}
}
