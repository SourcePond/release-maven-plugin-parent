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

import ch.sourcepond.maven.release.config.ParameterRegistration;
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
 * @since 1.4.0
 */
@Mojo(name = "next", requiresDirectInvocation = true, // this should not be
														// bound to a phase as
														// this plugin starts a
														// phase itself
inheritByDefault = true, // so you can configure this in a shared parent pom
requiresProject = true, // this can only run against a maven project
aggregator = true // the plugin should only run once against the aggregator pom
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
	@Parameter(property = "buildNumber")
	protected Long buildNumber;

	/**
	 * The modules to release, or no value to to release the project from the
	 * root pom, which is the default. The selected module plus any other
	 * modules it needs will be built and released also. When run from the
	 * command line, this can be a comma-separated list of module names.
	 */
	@Parameter(alias = "modulesToRelease", property = "modulesToRelease")
	protected List<String> modulesToRelease;

	/**
	 * A module to force release on, even if no changes has been detected.
	 */
	@Parameter(alias = "forceRelease", property = "forceRelease")
	protected List<String> modulesToForceRelease;

	@Parameter(property = "disableSshAgent")
	private boolean disableSshAgent;

	/**
	 * Specifies whether the release build should run with the "-X" switch.
	 */
	@Parameter(property = "debugEnabled")
	protected boolean debugEnabled;

	@Parameter(defaultValue = "${settings}", readonly = true, required = true)
	private Settings settings;

	/**
	 * If set, the identityFile and passphrase will be read from the Maven
	 * settings file.
	 */
	@Parameter(property = "serverId")
	private String serverId;

	/**
	 * If set, this file will be used to specify the known_hosts. This will
	 * override any default value.
	 */
	@Parameter(property = "knownHosts")
	private String knownHosts;

	/**
	 * Specifies the private key to be used.
	 */
	@Parameter(property = "privateKey")
	private String privateKey;

	/**
	 * Specifies the passphrase to be used with the identityFile specified.
	 */
	@Parameter(property = "passphrase")
	private String passphrase;

	private final ReactorFactory reactorFactory;
	protected final SCMRepository repository;
	private final MavenComponentSingletons singletons;
	private final ParameterRegistration registration;
	protected RootProject rootProject;

	@Inject
	public NextMojo(final SCMRepository pRepository, final ReactorFactory pReactorFactory,
			final MavenComponentSingletons pSingletons, final RootProject pRootProject,
			final ParameterRegistration pRegistration) {
		repository = pRepository;
		reactorFactory = pReactorFactory;
		singletons = pSingletons;
		rootProject = pRootProject;
		registration = pRegistration;
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
		final ProposedTagsBuilder builder = repository.newProposedTagsBuilder(rootProject.getRemoteUrlOrNull());
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

	protected ReactorFactory configureReactorFactory() {
		return reactorFactory.setBuildNumber(buildNumber).setModulesToForceRelease(modulesToForceRelease);
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

	protected void registerParemeters(final ParameterRegistration pRegistration) {
		pRegistration.setBuildNumber(buildNumber);
		pRegistration.setDebugEnabled(debugEnabled);
		pRegistration.setDisableSshAgent(disableSshAgent);
		pRegistration.setKnownHosts(knownHosts);
		pRegistration.setModulesToForceRelease(modulesToForceRelease);
		pRegistration.setModulesToRelease(modulesToRelease);
		pRegistration.setPassphrase(passphrase);
		pRegistration.setPrivateKey(privateKey);
		pRegistration.setServerId(serverId);
		pRegistration.setSettings(settings);
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			// Register singletons for injection
			singletons.initialize(getLog(), project, projects);

			// Register parameters for usage with Configuration
			registerParemeters(registration);
			repository.errorIfNotClean();
			configureJsch();
			final Reactor reactor = configureReactorFactory().newReactor();
			execute(reactor, figureOutTagNamesAndThrowIfAlreadyExists(reactor));
		} catch (final PluginException e) {
			e.printBigErrorMessageAndThrow(getLog());
		}
	}
}