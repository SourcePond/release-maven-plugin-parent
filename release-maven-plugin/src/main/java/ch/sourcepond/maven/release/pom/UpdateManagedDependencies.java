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

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.logging.Log;

import ch.sourcepond.maven.release.substitution.VersionSubstitution;

/**
 * @author rolandhauser
 *
 */
@Named("UpdateManagedDependencies")
@Singleton
final class UpdateManagedDependencies extends UpdateDependencies {

	@Inject
	UpdateManagedDependencies(final Log log, final VersionSubstitution pSubstitution) {
		super(log, pSubstitution);
	}

	@Override
	protected List<Dependency> determineDependencies(final Model model) {
		List<Dependency> dependencies = Collections.emptyList();
		final DependencyManagement mgmt = model.getDependencyManagement();

		if (mgmt != null) {
			dependencies = mgmt.getDependencies();
		}
		return dependencies;
	}

	@Override
	protected Integer priority() {
		return 0;
	}
}
