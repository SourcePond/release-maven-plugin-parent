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

import static java.lang.String.format;

import java.util.LinkedList;
import java.util.List;

import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.project.MavenProject;

import ch.sourcepond.maven.release.commons.Version;
import ch.sourcepond.maven.release.reactor.Reactor;
import ch.sourcepond.maven.release.reactor.ReleasableModule;
import ch.sourcepond.maven.release.reactor.UnresolvedSnapshotDependencyException;


class Context {

	private final List<String> errors = new LinkedList<>();
	private final Reactor reactor;
	private final Model model;
	private final MavenProject project;
	private final boolean incrementSnapshotVersionAfterRelease;
	private boolean needsOwnVersion;
	private boolean dependencyUpdated;

	Context(final Reactor reactor, final MavenProject project, final Model model,
			final boolean incrementSnapshotVersionAfterRelease) {
		this.reactor = reactor;
		this.project = project;
		this.model = model;
		this.incrementSnapshotVersionAfterRelease = incrementSnapshotVersionAfterRelease;
	}

	public void addError(final String format, final Object... args) {
		errors.add(format(format, args));
	}

	public Model getModel() {
		return model;
	}

	public MavenProject getProject() {
		return project;
	}

	public List<String> getErrors() {
		return errors;
	}

	public String getVersionToDependOn(final String groupId, final String artifactId)
			throws UnresolvedSnapshotDependencyException {
		final Version version = reactor.find(groupId, artifactId).getVersion();

		String versionToDependOn = null;
		if (incrementSnapshotVersionAfterRelease) {
			if (version.hasChanged()) {
				versionToDependOn = version.getNextDevelopmentVersion();
			} else {
				versionToDependOn = version.getDevelopmentVersion();
			}
		} else if (version.getEquivalentVersionOrNull() == null) {
			versionToDependOn = version.getReleaseVersion();
		} else {
			versionToDependOn = version.getEquivalentVersionOrNull();
		}

		return versionToDependOn;
	}

	public boolean needsOwnVersion() {
		return needsOwnVersion;
	}

	public void setNeedsOwnVersion(final boolean pNeedsOwnVersion) {
		needsOwnVersion = pNeedsOwnVersion;
	}

	public boolean hasNotChanged(final Parent pParentOrNull) {
		boolean hasNotChanged = true;
		if (pParentOrNull != null) {
			final ReleasableModule module = reactor
					.findByLabel(pParentOrNull.getGroupId() + ":" + pParentOrNull.getArtifactId());
			hasNotChanged = module == null || !module.getVersion().hasChanged();
		}

		return hasNotChanged;
	}

	public boolean dependencyUpdated() {
		return dependencyUpdated;
	}

	public void setDependencyUpdated() {
		if (!dependencyUpdated) {
			dependencyUpdated = true;
		}
	}
}