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
package ch.sourcepond.maven.release.providers;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.model.Scm;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.lib.Repository;

import ch.sourcepond.maven.release.commons.PluginException;
import ch.sourcepond.maven.release.reactor.ReactorException;

@Named
@Singleton
class DefaultRootProject implements RootProject, Initializable {
	static final String ERROR_SUMMARY = "Cannot run the release plugin with a non-Git version control system";
	static final String GIT_PREFIX = "scm:git:";
	private final MavenComponentSingletons singletons;
	private String remoteUrlOrNull;

	@Inject
	DefaultRootProject(final MavenComponentSingletons pSingletons) {
		singletons = pSingletons;
	}

	private MavenProject getProject() {
		return singletons.getProject();
	}

	@Override
	public void initialize() throws PluginException {
		final Scm scm = getProject().getScm();
		if (scm != null) {
			remoteUrlOrNull = scm.getDeveloperConnection();
			if (remoteUrlOrNull == null) {
				remoteUrlOrNull = scm.getConnection();
			}
			if (remoteUrlOrNull != null) {
				if (!remoteUrlOrNull.startsWith(GIT_PREFIX)) {
					throw new PluginException(ERROR_SUMMARY).add("The value in your scm tag is %s", remoteUrlOrNull);
				}
				remoteUrlOrNull = remoteUrlOrNull.substring(GIT_PREFIX.length()).replace("file://localhost/",
						"file:///");
			}
		}
	}

	@Override
	public Collection<String> getActiveProfileIds() {
		final List<String> ids = new LinkedList<>();
		for (final Object activatedProfile : getProject().getActiveProfiles()) {
			ids.add(((org.apache.maven.model.Profile) activatedProfile).getId());
		}
		return ids;
	}

	@Override
	public String calculateModulePath(final MavenProject project) throws ReactorException {
		// Getting canonical files because on Windows, it's possible one returns
		// "C:\..." and the other "c:\..." which is rather amazing
		File projectRoot;
		File moduleRoot;
		try {
			projectRoot = getProject().getBasedir().getCanonicalFile();
			moduleRoot = project.getBasedir().getCanonicalFile();
		} catch (final IOException e) {
			throw new ReactorException(e, "Could not find directory paths for maven project");
		}
		String relativePathToModule = Repository.stripWorkDir(projectRoot, moduleRoot);
		if (relativePathToModule.length() == 0) {
			relativePathToModule = ".";
		}
		return relativePathToModule;
	}

	@Override
	public String getRemoteUrlOrNull() {
		return remoteUrlOrNull;
	}
}
