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

import static java.lang.String.format;

import java.util.Collection;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import ch.sourcepond.maven.release.scm.ProposedTag;
import ch.sourcepond.maven.release.scm.SCMException;
import ch.sourcepond.maven.release.scm.SCMRepository;

class ChangeDetector {
	private final Log log;
	private final SCMRepository repository;
	private String relativePathToModule;
	private MavenProject project;
	private long buildNumber;
	private String changedDependency;
	private String businessVersion;

	ChangeDetector(final Log log, final SCMRepository repository) {
		this.log = log;
		this.repository = repository;
	}

	public ChangeDetector setActualBuildNumber(final long buildNumber) {
		this.buildNumber = buildNumber;
		return this;
	}

	public ChangeDetector setProject(final MavenProject project) {
		this.project = project;
		return this;
	}

	public ChangeDetector setChangedDependency(final String changedDependency) {
		this.changedDependency = changedDependency;
		return this;
	}

	public ChangeDetector setBusinessVersion(final String businessVersion) {
		this.businessVersion = businessVersion;
		return this;
	}

	public ChangeDetector setRelativePathToModule(final String relativePathToModule) {
		this.relativePathToModule = relativePathToModule;
		return this;
	}

	public String equivalentVersionOrNull() throws VersionException {
		final String releaseVersion = businessVersion + "." + buildNumber;

		if (relativePathToModule == null) {
			log.info(format("Releasing %s %s as we was asked to forced release.", project.getArtifactId(),
					releaseVersion));
		}

		if (changedDependency != null) {
			log.info(format("Releasing %s %s as %s has changed.", project.getArtifactId(), releaseVersion,
					changedDependency));
		}

		String equivalentVersionOrNull = null;
		if (relativePathToModule != null && changedDependency == null) {
			final ProposedTag previousTagThatIsTheSameAsHEADForThisModule = hasChangedSinceLastRelease();
			if (previousTagThatIsTheSameAsHEADForThisModule != null) {
				equivalentVersionOrNull = previousTagThatIsTheSameAsHEADForThisModule.getReleaseVersion();
				log.info(format("Will use version %s for %s as it has not been changed since that release.",
						equivalentVersionOrNull, project.getArtifactId()));
			} else {
				log.info(format("Will use version %s for %s as it has changed since the last release.", releaseVersion,
						project.getArtifactId()));
			}
		}

		return equivalentVersionOrNull;
	}

	private ProposedTag hasChangedSinceLastRelease() throws VersionException {
		try {
			final Collection<ProposedTag> previousTagsForThisModule = repository.tagsForVersion(project.getArtifactId(),
					businessVersion);
			if (previousTagsForThisModule.size() == 0) {
				return null;
			}
			final boolean hasChanged = repository.hasChangedSince(relativePathToModule, project.getModules(),
					previousTagsForThisModule);
			return hasChanged ? null : tagWithHighestBuildNumber(previousTagsForThisModule);
		} catch (final SCMException e) {
			throw new VersionException(e, "Error while detecting whether or not %s has changed since the last release",
					project.getArtifactId());
		}
	}

	private ProposedTag tagWithHighestBuildNumber(final Collection<ProposedTag> previousTagsForThisModule) {
		ProposedTag cur = null;
		for (final ProposedTag tag : previousTagsForThisModule) {
			if (cur == null || tag.getBuildNumber() > cur.getBuildNumber()) {
				cur = tag;
			}
		}
		return cur;
	}
}
