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

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.model.Model;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import ch.sourcepond.maven.release.reactor.UnresolvedSnapshotDependencyException;

@Named("UpdateModel")
@Singleton
final class UpdateModel extends Command {
	static final String ERROR_FORMAT = "Project not found in reactor: %s";

	@Inject
	UpdateModel(final Log pLog) {
		super(pLog);
	}

	@Override
	public void alterModel(final Context updateContext) {
		final MavenProject project = updateContext.getProject();
		final Model model = updateContext.getModel();

		final boolean needsOwnVersion = isBlank(model.getVersion()) && updateContext.hasNotChanged(model.getParent())
				&& updateContext.dependencyUpdated();
		updateContext.setNeedsOwnVersion(needsOwnVersion);

		// Do only update version on model when it is explicitly set. Otherwise,
		// the version of the parent is used.
		if (isNotBlank(model.getVersion()) || needsOwnVersion) {
			try {
				model.setVersion(updateContext.getVersionToDependOn(project.getGroupId(), project.getArtifactId()));
			} catch (final UnresolvedSnapshotDependencyException e) {
				updateContext.addError(ERROR_FORMAT, project);
			}
		}
	}

	@Override
	protected Integer priority() {
		return 3;
	}
}