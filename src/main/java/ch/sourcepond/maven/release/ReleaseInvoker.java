package ch.sourcepond.maven.release;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
	static final String DEPLOY = "deploy";
	static final String SKIP_TESTS = "-DskipTests=true";
	static final String LOCAL_REPO = "-Dmaven.repo.local=%s";
	private final Log log;
	private final RootProject project;
	private final InvocationRequest request;
	private final Invoker invoker;
	private boolean skipTests;
	private boolean debugEnabled;
	private File localMavenRepo;
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
		return modulesToRelease == null ? Collections.<String> emptyList() : modulesToRelease;
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

	final void setLocalMavenRepo(final File localMavenRepo) {
		this.localMavenRepo = localMavenRepo;
	}

	public final void runMavenBuild(final Reactor reactor) throws MojoExecutionException {
		request.setInteractive(false);
		request.setShowErrors(true);
		request.setDebug(debugEnabled || log.isDebugEnabled());

		final List<String> goals = getGoals();
		if (skipTests) {
			goals.add(SKIP_TESTS);
		}
		if (localMavenRepo != null) {
			try {
				goals.add(format(LOCAL_REPO, localMavenRepo.getCanonicalFile()));
			} catch (final IOException e) {
				throw new MojoExecutionException("Local repository path could not be determined!", e);
			}
		}
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
