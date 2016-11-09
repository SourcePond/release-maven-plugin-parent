package ch.sourcepond.maven.release;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import ch.sourcepond.maven.release.commons.PluginException;
import ch.sourcepond.maven.release.config.Configuration;
import ch.sourcepond.maven.release.pom.ChangeSet;
import ch.sourcepond.maven.release.pom.Updater;
import ch.sourcepond.maven.release.providers.MavenComponentSingletons;
import ch.sourcepond.maven.release.providers.RootProject;
import ch.sourcepond.maven.release.reactor.Reactor;
import ch.sourcepond.maven.release.reactor.ReactorFactory;
import ch.sourcepond.maven.release.scm.ProposedTags;
import ch.sourcepond.maven.release.scm.SCMRepository;

/**
 * Releases the project.
 */
@Mojo(name = "release", requiresDirectInvocation = true, // this should not be
															// bound to a phase
															// as this plugin
															// starts a phase
															// itself
		inheritByDefault = true, // so you can configure this in a shared parent
									// pom
		requiresProject = true, // this can only run against a maven project
		aggregator = true // the plugin should only run once against the
							// aggregator pom
)
public class ReleaseMojo extends NextMojo {

	/**
	 * <p>
	 * The goals to run against the project during a release. By default this is
	 * "deploy" which means the release version of your artifact will be tested
	 * and deployed.
	 * </p>
	 * <p>
	 * You can specify more goals and maven options. For example if you want to
	 * perform a clean, build a maven site, and then deploys it, use:
	 * </p>
	 * 
	 * <pre>
	 * {@code
	 * <releaseGoals>
	 *     <releaseGoal>clean</releaseGoal>
	 *     <releaseGoal>site</releaseGoal>
	 *     <releaseGoal>deploy</releaseGoal>
	 * </releaseGoals>
	 * }
	 * </pre>
	 */
	@Parameter(alias = "releaseGoals")
	private List<String> goals;

	/**
	 * <p>
	 * Profiles to activate during the release.
	 * </p>
	 * <p>
	 * Note that if any profiles are activated during the build using the `-P`
	 * or `--activate-profiles` will also be activated during release. This
	 * gives two options for running releases: either configure it in the plugin
	 * configuration, or activate profiles from the command line.
	 * </p>
	 * 
	 * @since 1.0.0
	 */
	@Parameter(alias = "releaseProfiles")
	private List<String> releaseProfiles;

	@Parameter(property = "incrementSnapshotVersionAfterRelease")
	private boolean incrementSnapshotVersionAfterRelease;

	/**
	 * If true then tests will not be run during a release. This is the same as
	 * adding -DskipTests=true to the release goals.
	 */
	@Parameter(alias = "skipTests", defaultValue = "false", property = "skipTests")
	private boolean skipTests;

	/**
	 * Specifies a custom, user specific Maven settings file to be used during
	 * the release build.
	 */
	@Parameter(property = "userSettings")
	private File userSettings;

	/**
	 * Specifies a custom, global Maven settings file to be used during the
	 * release build.
	 */
	@Parameter(property = "globalSettings")
	private File globalSettings;

	/**
	 * Specifies a custom directory which should be used as local Maven
	 * repository.
	 */
	@Parameter(property = "localMavenRepo")
	private File localMavenRepo;

	/**
	 * Specifies whether the plugin should push changes to the remote
	 * repository. This property has only an effect, when a distributed SCM like
	 * GIT is used. If {@code remoteRepositoryEnabled} is disabled and a
	 * distributed SCM like GIT is used, this property has no effect.
	 */
	@Parameter(property = Configuration.REMOTE_PUSH_ENABLED, defaultValue = "true")
	private boolean remotePushEnabled;

	private final Updater updater;

	@Inject
	public ReleaseMojo(final SCMRepository pRepository, final ReactorFactory pBuilderFactory,
			final MavenComponentSingletons singletons, final RootProject pRootProject, final Updater pUpdater) {
		super(pRepository, pBuilderFactory, singletons, pRootProject);
		updater = pUpdater;
	}

	@Override
	protected void execute(final Reactor reactor, final ProposedTags proposedTags)
			throws MojoExecutionException, PluginException {
		try (final ChangeSet changedFiles = updater.updatePoms(reactor, incrementSnapshotVersionAfterRelease)) {
			try {
				// Do this before running the maven build in case the build
				// uploads
				// some artifacts and then fails. If it is
				// not tagged in a half-failed build, then subsequent releases
				// will
				// re-use a version that is already in Nexus
				// and so fail. The downside is that failed builds result in
				// tags
				// being pushed.
				proposedTags.tag();

				final ReleaseInvoker invoker = new ReleaseInvoker(getLog(), rootProject);
				invoker.setGlobalSettings(globalSettings);
				invoker.setUserSettings(userSettings);
				invoker.setLocalMavenRepo(localMavenRepo);
				invoker.setGoals(goals);
				invoker.setModulesToRelease(modulesToRelease);
				invoker.setReleaseProfiles(releaseProfiles);
				invoker.setSkipTests(skipTests);
				invoker.setDebugEnabled(debugEnabled);
				invoker.runMavenBuild(reactor);
			} catch (final Exception e) {
				try {
					proposedTags.undoTag();
				} finally {
					changedFiles.setFailure("Exception occurred while release invokation!", e);
				}
			}
		}
	}
}