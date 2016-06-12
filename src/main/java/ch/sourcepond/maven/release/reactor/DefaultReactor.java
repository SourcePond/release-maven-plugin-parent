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
package ch.sourcepond.maven.release.reactor;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

final class DefaultReactor implements Reactor {
	private final List<ReleasableModule> modulesInBuildOrder = new LinkedList<ReleasableModule>();
	private final Log log;

	DefaultReactor(final Log log) {
		this.log = log;
	}

	void addReleasableModule(final ReleasableModule module) {
		modulesInBuildOrder.add(module);
	}

	Reactor finalizeReleaseVersions() {
		if (!atLeastOneBeingReleased()) {
			log.warn("No changes have been detected in any modules so will re-release them all");
			for (final ReleasableModule module : modulesInBuildOrder) {
				module.getVersion().makeReleaseable();
			}
		}
		return this;
	}

	private boolean atLeastOneBeingReleased() {
		for (final ReleasableModule module : this) {
			if (module.getVersion().hasChanged()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public ReleasableModule findByLabel(final String label) {
		for (final ReleasableModule module : modulesInBuildOrder) {
			final String currentLabel = module.getGroupId() + ":" + module.getArtifactId();
			if (currentLabel.equals(label)) {
				return module;
			}
		}
		return null;
	}

	@Override
	public ReleasableModule find(final String groupId, final String artifactId)
			throws UnresolvedSnapshotDependencyException {
		final ReleasableModule value = findByLabel(groupId + ":" + artifactId);
		if (value == null) {
			throw new UnresolvedSnapshotDependencyException(groupId, artifactId);
		}
		return value;
	}

	public String getChangedDependencyOrNull(final MavenProject project) {
		String changedDependency = null;
		for (final ReleasableModule module : this) {
			if (module.getVersion().hasChanged()) {
				for (final Dependency dependency : project.getDependencies()) {
					if (dependency.getGroupId().equals(module.getGroupId())
							&& dependency.getArtifactId().equals(module.getArtifactId())) {
						changedDependency = dependency.getArtifactId();
						break;
					}
				}
				if (project.getParent() != null && (project.getParent().getGroupId().equals(module.getGroupId())
						&& project.getParent().getArtifactId().equals(module.getArtifactId()))) {
					changedDependency = project.getParent().getArtifactId();
					break;
				}
			}
			if (changedDependency != null) {
				break;
			}
		}
		return changedDependency;
	}

	@Override
	public Iterator<ReleasableModule> iterator() {
		return modulesInBuildOrder.iterator();
	}
}
