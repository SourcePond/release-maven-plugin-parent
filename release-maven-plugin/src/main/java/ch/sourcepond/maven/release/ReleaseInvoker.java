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
package ch.sourcepond.maven.release;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

import ch.sourcepond.maven.release.providers.RootProject;
import ch.sourcepond.maven.release.reactor.Reactor;
import ch.sourcepond.maven.release.reactor.ReleasableModule;

/**
 * @author Roland Hauser sourcepond@gmail.com
 *
 */
class ReleaseInvoker {
	static final String[] DEFAULT_VM_PROPERTY_NAMES = new String[] { "java.version", "java.vendor", "java.vendor.url",
			"java.home", "java.vm.specification.version", "java.vm.specification.vendor", "java.vm.specification.name",
			"java.vm.version", "java.vm.vendor", "java.vm.name", "java.specification.version",
			"java.specification.vendor", "java.specification.name", "java.class.version", "java.class.path",
			"java.library.path", "java.io.tmpdir", "java.compiler", "java.ext.dirs", "os.name", "os.arch",
			"os.version	Operating", "file.separator", "path.separator", "line.separator", "user.name", "user.home",
			"user.dir", "file.encoding" };
	static final String DEPLOY = "deploy";
	static final String SKIP_TESTS = "skipTests";
	static final String DEPLOY_AT_END = "deployAtEnd";
	private final Log log;
	private final RootProject project;
	private final InvocationRequest request;
	private final Invoker invoker;
	private boolean skipTests;
	private boolean debugEnabled;
	private List<String> goals;
	private List<String> modulesToRelease;
	private List<String> releaseProfiles;

	public ReleaseInvoker(final Log log, final RootProject project) {
		this(log, project, new DefaultInvocationRequest(), new DefaultInvoker());
	}

	public ReleaseInvoker(final Log log, final RootProject project, final InvocationRequest request,
			final Invoker invoker) {
		this.log = log;
		this.project = project;
		this.request = request;
		this.invoker = invoker;
	}

	private List<String> getGoals() {
		if (goals == null || goals.isEmpty()) {
			goals = new ArrayList<String>();
			goals.add(DEPLOY);
		}
		return goals;
	}

	private List<String> getModulesToRelease() {
		return modulesToRelease == null ? Collections.<String>emptyList() : modulesToRelease;
	}

	private List<String> getReleaseProfilesOrNull() {
		return releaseProfiles;
	}

	final void setGoals(final List<String> goalsOrNull) {
		goals = goalsOrNull;
	}

	final void setModulesToRelease(final List<String> modulesToReleaseOrNull) {
		modulesToRelease = modulesToReleaseOrNull;
	}

	final void setReleaseProfiles(final List<String> releaseProfilesOrNull) {
		releaseProfiles = releaseProfilesOrNull;
	}

	final void setDebugEnabled(final boolean debugEnabled) {
		this.debugEnabled = debugEnabled;
	}

	final void setSkipTests(final boolean skipTests) {
		this.skipTests = skipTests;
	}

	final void setGlobalSettings(final File globalSettings) {
		request.setGlobalSettingsFile(globalSettings);
	}

	final void setUserSettings(final File userSettings) {
		request.setUserSettingsFile(userSettings);
	}

	final void setLocalMavenRepo(final File localMavenRepo) throws MojoExecutionException {
		if (localMavenRepo != null) {
			try {
				request.setLocalRepositoryDirectory(localMavenRepo.getCanonicalFile());
			} catch (final IOException e) {
				throw new MojoExecutionException("Local repository path could not be determined!", e);
			}
		}
	}

	private Properties cloneSystemProperties() {
		final Properties env = (Properties) System.getProperties().clone();
		for (final String defaultVmPropertyName : DEFAULT_VM_PROPERTY_NAMES) {
			env.remove(defaultVmPropertyName);
		}

		// This enforces maven-deploy-plugin to upload generated artifact AFTER
		// all modules have been built. Requires at least maven-deploy-plugin
		// 2.8 otherwise this parameter will be ignored and the artifacts will be
		// uploaded during each sub-module build.
		env.setProperty(DEPLOY_AT_END, "true");
		return env;
	}

	public final void runMavenBuild(final Reactor reactor) throws MojoExecutionException {
		request.setInteractive(false);
		request.setShowErrors(true);
		request.setDebug(debugEnabled || log.isDebugEnabled());

		final Properties env = cloneSystemProperties();
		if (skipTests) {
			env.put(SKIP_TESTS, String.valueOf(true));
		}

		request.setProperties(env);
		request.setGoals(getGoals());

		final List<String> profiles = profilesToActivate();
		request.setProfiles(profiles);

		final List<String> changedModules = new ArrayList<String>();
		final List<String> modulesToRelease = getModulesToRelease();
		for (final ReleasableModule releasableModule : reactor) {
			final String modulePath = releasableModule.getRelativePathToModule();
			final boolean userExplicitlyWantsThisToBeReleased = modulesToRelease.contains(modulePath);
			final boolean userImplicitlyWantsThisToBeReleased = modulesToRelease.isEmpty();
			if (userExplicitlyWantsThisToBeReleased
					|| (userImplicitlyWantsThisToBeReleased && releasableModule.getVersion().hasChanged())) {
				changedModules.add(modulePath);
			}
		}

		request.setProjects(changedModules);

		final String profilesInfo = profiles.isEmpty() ? "no profiles activated" : "profiles " + profiles;

		log.info(format("About to run mvn %s with %s and modules %s", goals, profilesInfo, changedModules));

		try {
			final InvocationResult result = invoker.execute(request);
			if (result.getExitCode() != 0) {
				throw new MojoExecutionException("Maven execution returned code " + result.getExitCode());
			}
		} catch (final MavenInvocationException e) {
			throw new MojoExecutionException("Failed to build artifact", e);
		}
	}

	private List<String> profilesToActivate() {
		final List<String> profiles = new ArrayList<String>();
		if (getReleaseProfilesOrNull() != null) {
			for (final String releaseProfile : getReleaseProfilesOrNull()) {
				profiles.add(releaseProfile);
			}
		}
		profiles.addAll(project.getActiveProfileIds());
		return profiles;
	}
}
