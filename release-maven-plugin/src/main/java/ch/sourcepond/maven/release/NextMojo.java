package ch.sourcepond.maven.release;

import static java.lang.String.format;

import java.util.List;

import javax.inject.Inject;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.eclipse.jgit.transport.JschConfigSessionFactory;

import ch.sourcepond.maven.release.commons.PluginException;
import ch.sourcepond.maven.release.config.Configuration;
import ch.sourcepond.maven.release.providers.MavenComponentSingletons;
import ch.sourcepond.maven.release.providers.RootProject;
import ch.sourcepond.maven.release.reactor.Reactor;
import ch.sourcepond.maven.release.reactor.ReactorFactory;
import ch.sourcepond.maven.release.reactor.ReleasableModule;
import ch.sourcepond.maven.release.scm.ProposedTags;
import ch.sourcepond.maven.release.scm.ProposedTagsBuilder;
import ch.sourcepond.maven.release.scm.SCMRepository;

/**
 * Logs the versions of the modules that the releaser will release on the next
 * release. Does not run the build nor tag the repo.
 * 
 * @since 1.0.0
 */
@Mojo(name = "next", requiresDirectInvocation = true, // this should not be
														// bound to a phase as
														// this plugin starts a
														// phase itself
		inheritByDefault = true, // so you can configure this in a shared parent
									// pom
		requiresProject = true, // this can only run against a maven project
		aggregator = true // the plugin should only run once against the
							// aggregator pom
)
public class NextMojo extends AbstractMojo {

	/**
	 * The Maven Project.
	 */
	@Parameter(property = "project", required = true, readonly = true, defaultValue = "${project}")
	protected MavenProject project;

	@Parameter(property = "projects", required = true, readonly = true, defaultValue = "${reactorProjects}")
	protected List<MavenProject> projects;

	/**
	 * <p>
	 * The build number to use in the release version. Given a snapshot version
	 * of "1.0-SNAPSHOT" and a buildNumber value of "2", the actual released
	 * version will be "1.0.2".
	 * </p>
	 * <p>
	 * By default, the plugin will automatically find a suitable build number.
	 * It will start at version 0 and increment this with each release.
	 * </p>
	 * <p>
	 * This can be specified using a command line parameter ("-DbuildNumber=2")
	 * or in this plugin's configuration.
	 * </p>
	 */
	@Parameter(property = Configuration.BUILD_NUMBER)
	protected Long buildNumber;

	/**
	 * The modules to release, or no value to to release the project from the
	 * root pom, which is the default. The selected module plus any other
	 * modules it needs will be built and released also. When run from the
	 * command line, this can be a comma-separated list of module names.
	 */
	@Parameter(alias = "modulesToRelease", property = Configuration.MODULES_TO_RELEASE)
	protected List<String> modulesToRelease;

	/**
	 * A module to force release on, even if no changes has been detected.
	 */
	@Parameter(alias = "forceRelease", property = Configuration.MODULES_TO_FORCE_RELEASE)
	protected List<String> modulesToForceRelease;

	@Parameter(property = Configuration.DISABLE_SSH_AGENT)
	private boolean disableSshAgent;

	/**
	 * Specifies whether the release build should run with the "-X" switch.
	 */
	@Parameter(property = Configuration.DEBUG_ENABLED)
	protected boolean debugEnabled;

	@Parameter(defaultValue = "${settings}", readonly = true, required = true)
	private Settings settings;

	/**
	 * If set, the identityFile and passphrase will be read from the Maven
	 * settings file.
	 */
	@Parameter(property = Configuration.SERVER_ID)
	private String serverId;

	/**
	 * If set, this file will be used to specify the known_hosts. This will
	 * override any default value.
	 */
	@Parameter(property = Configuration.KNOWN_HOSTS)
	private String knownHosts;

	/**
	 * Specifies the private key to be used.
	 */
	@Parameter(property = Configuration.PRIVATE_KEY)
	private String privateKey;

	/**
	 * Specifies the passphrase to be used with the identityFile specified.
	 */
	@Parameter(property = Configuration.PASSPHRASE)
	private String passphrase;

	/**
	 * Specifies whether the plugin should also work with the remote repository.
	 * If {@code true}, the remote repository will be taken into account during
	 * the release process. This property has only an effect, when a distributed
	 * SCM like GIT is used. On client/server SCMs like Subversion, this
	 * property is ignored. Default value is {@code true}.
	 */
	@Parameter(property = Configuration.REMOTE_REPOSITORY_ENABLED, defaultValue = "true")
	private boolean remoteRepositoryEnabled;

	private final ReactorFactory reactorFactory;
	protected final SCMRepository repository;
	private final MavenComponentSingletons singletons;
	protected RootProject rootProject;

	@Inject
	public NextMojo(final SCMRepository pRepository, final ReactorFactory pReactorFactory,
			final MavenComponentSingletons pSingletons, final RootProject pRootProject) {
		repository = pRepository;
		reactorFactory = pReactorFactory;
		singletons = pSingletons;
		rootProject = pRootProject;
	}

	final void setSettings(final Settings settings) {
		this.settings = settings;
	}

	final void setServerId(final String serverId) {
		this.serverId = serverId;
	}

	final void setKnownHosts(final String knownHosts) {
		this.knownHosts = knownHosts;
	}

	final void setPrivateKey(final String privateKey) {
		this.privateKey = privateKey;
	}

	final void setPassphrase(final String passphrase) {
		this.passphrase = passphrase;
	}

	final void disableSshAgent() {
		disableSshAgent = true;
	}

	protected ProposedTags figureOutTagNamesAndThrowIfAlreadyExists(final Reactor reactor) throws PluginException {
		final ProposedTagsBuilder builder = repository.newProposedTagsBuilder();
		for (final ReleasableModule module : reactor) {
			if (!module.getVersion().hasChanged()) {
				continue;
			}
			if (modulesToRelease == null || modulesToRelease.size() == 0 || module.isOneOf(modulesToRelease)) {
				builder.add(module.getTagName(), module.getVersion());
			}
		}
		return builder.build();
	}

	protected final void configureJsch() {
		if (!disableSshAgent) {
			if (serverId != null) {
				final Server server = settings.getServer(serverId);
				if (server != null) {
					privateKey = privateKey == null ? server.getPrivateKey() : privateKey;
					passphrase = passphrase == null ? server.getPassphrase() : passphrase;
				} else {
					getLog().warn(format("No server configuration in Maven settings found with id %s", serverId));
				}
			}

			JschConfigSessionFactory
					.setInstance(new SshAgentSessionFactory(getLog(), knownHosts, privateKey, passphrase));
		}
	}

	protected void execute(final Reactor reactor, final ProposedTags proposedTags)
			throws MojoExecutionException, PluginException {
		// noop by default
	}

	@Override
	public final void execute() throws MojoExecutionException, MojoFailureException {
		try {
			// Register singletons for injection
			singletons.initialize(this, project, projects);

			repository.errorIfNotClean();
			configureJsch();
			final Reactor reactor = reactorFactory.newReactor();
			execute(reactor, figureOutTagNamesAndThrowIfAlreadyExists(reactor));
		} catch (final PluginException e) {
			e.printBigErrorMessageAndThrow(getLog());
		}
	}
}