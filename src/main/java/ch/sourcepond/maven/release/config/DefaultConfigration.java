package ch.sourcepond.maven.release.config;

import java.io.File;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.settings.Settings;

@Named
@Singleton
final class DefaultConfigration implements ParameterRegistration, Configuration {

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
	private Long buildNumber;

	/**
	 * The modules to release, or no value to to release the project from the
	 * root pom, which is the default. The selected module plus any other
	 * modules it needs will be built and released also. When run from the
	 * command line, this can be a comma-separated list of module names.
	 */
	private List<String> modulesToRelease;

	/**
	 * A module to force release on, even if no changes has been detected.
	 */
	private List<String> modulesToForceRelease;

	private boolean disableSshAgent;

	/**
	 * Specifies whether the release build should run with the "-X" switch.
	 */
	private boolean debugEnabled;

	/**
	 * Specifies whether the release build should run with the "-e" switch.
	 */
	private boolean stacktraceEnabled;

	private Settings settings;

	/**
	 * If set, the identityFile and passphrase will be read from the Maven
	 * settings file.
	 */
	private String serverId;

	/**
	 * If set, this file will be used to specify the known_hosts. This will
	 * override any default value.
	 */
	private String knownHosts;

	/**
	 * Specifies the private key to be used.
	 */
	private String privateKey;

	/**
	 * Specifies the passphrase to be used with the identityFile specified.
	 */
	private String passphrase;

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
	 * @since 1.0.1
	 */
	private List<String> releaseProfiles;

	private boolean incrementSnapshotVersionAfterRelease;

	/**
	 * If true then tests will not be run during a release. This is the same as
	 * adding -DskipTests=true to the release goals.
	 */
	private boolean skipTests;

	/**
	 * Specifies a custom, user specific Maven settings file to be used during
	 * the release build.
	 */
	private File userSettings;

	/**
	 * Specifies a custom, global Maven settings file to be used during the
	 * release build.
	 */
	private File globalSettings;

	/**
	 * Specifies a custom directory which should be used as local Maven
	 * repository.
	 */
	private File localMavenRepo;

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.sourcepond.maven.release.config.Configuration#getBuildNumber()
	 */
	@Override
	public Long getBuildNumber() {
		return buildNumber;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.sourcepond.maven.release.config.ParameterRegistration#setBuildNumber(
	 * java.lang.Long)
	 */
	@Override
	public void setBuildNumber(final Long buildNumber) {
		this.buildNumber = buildNumber;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.sourcepond.maven.release.config.Configuration#getModulesToRelease()
	 */
	@Override
	public List<String> getModulesToRelease() {
		return modulesToRelease;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.sourcepond.maven.release.config.ParameterRegistration#
	 * setModulesToRelease(java.util.List)
	 */
	@Override
	public void setModulesToRelease(final List<String> modulesToRelease) {
		this.modulesToRelease = modulesToRelease;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.sourcepond.maven.release.config.Configuration#getModulesToForceRelease
	 * ()
	 */
	@Override
	public List<String> getModulesToForceRelease() {
		return modulesToForceRelease;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.sourcepond.maven.release.config.ParameterRegistration#
	 * setModulesToForceRelease(java.util.List)
	 */
	@Override
	public void setModulesToForceRelease(final List<String> modulesToForceRelease) {
		this.modulesToForceRelease = modulesToForceRelease;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.sourcepond.maven.release.config.Configuration#isDisableSshAgent()
	 */
	@Override
	public boolean isDisableSshAgent() {
		return disableSshAgent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.sourcepond.maven.release.config.ParameterRegistration#
	 * setDisableSshAgent(boolean)
	 */
	@Override
	public void setDisableSshAgent(final boolean disableSshAgent) {
		this.disableSshAgent = disableSshAgent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.sourcepond.maven.release.config.Configuration#isDebugEnabled()
	 */
	@Override
	public boolean isDebugEnabled() {
		return debugEnabled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.sourcepond.maven.release.config.ParameterRegistration#setDebugEnabled(
	 * boolean)
	 */
	@Override
	public void setDebugEnabled(final boolean debugEnabled) {
		this.debugEnabled = debugEnabled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.sourcepond.maven.release.config.Configuration#isStacktraceEnabled()
	 */
	@Override
	public boolean isStacktraceEnabled() {
		return stacktraceEnabled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.sourcepond.maven.release.config.ParameterRegistration#
	 * setStacktraceEnabled(boolean)
	 */
	@Override
	public void setStacktraceEnabled(final boolean stacktraceEnabled) {
		this.stacktraceEnabled = stacktraceEnabled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.sourcepond.maven.release.config.Configuration#getSettings()
	 */
	@Override
	public Settings getSettings() {
		return settings;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.sourcepond.maven.release.config.ParameterRegistration#setSettings(org.
	 * apache.maven.settings.Settings)
	 */
	@Override
	public void setSettings(final Settings settings) {
		this.settings = settings;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.sourcepond.maven.release.config.Configuration#getServerId()
	 */
	@Override
	public String getServerId() {
		return serverId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.sourcepond.maven.release.config.ParameterRegistration#setServerId(java
	 * .lang.String)
	 */
	@Override
	public void setServerId(final String serverId) {
		this.serverId = serverId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.sourcepond.maven.release.config.Configuration#getKnownHosts()
	 */
	@Override
	public String getKnownHosts() {
		return knownHosts;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.sourcepond.maven.release.config.ParameterRegistration#setKnownHosts(
	 * java.lang.String)
	 */
	@Override
	public void setKnownHosts(final String knownHosts) {
		this.knownHosts = knownHosts;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.sourcepond.maven.release.config.Configuration#getPrivateKey()
	 */
	@Override
	public String getPrivateKey() {
		return privateKey;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.sourcepond.maven.release.config.ParameterRegistration#setPrivateKey(
	 * java.lang.String)
	 */
	@Override
	public void setPrivateKey(final String privateKey) {
		this.privateKey = privateKey;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.sourcepond.maven.release.config.Configuration#getPassphrase()
	 */
	@Override
	public String getPassphrase() {
		return passphrase;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.sourcepond.maven.release.config.ParameterRegistration#setPassphrase(
	 * java.lang.String)
	 */
	@Override
	public void setPassphrase(final String passphrase) {
		this.passphrase = passphrase;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.sourcepond.maven.release.config.Configuration#getGoals()
	 */
	@Override
	public List<String> getGoals() {
		return goals;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.sourcepond.maven.release.config.ParameterRegistration#setGoals(java.
	 * util.List)
	 */
	@Override
	public void setGoals(final List<String> goals) {
		this.goals = goals;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.sourcepond.maven.release.config.Configuration#getReleaseProfiles()
	 */
	@Override
	public List<String> getReleaseProfiles() {
		return releaseProfiles;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.sourcepond.maven.release.config.ParameterRegistration#
	 * setReleaseProfiles(java.util.List)
	 */
	@Override
	public void setReleaseProfiles(final List<String> releaseProfiles) {
		this.releaseProfiles = releaseProfiles;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.sourcepond.maven.release.config.Configuration#
	 * isIncrementSnapshotVersionAfterRelease()
	 */
	@Override
	public boolean isIncrementSnapshotVersionAfterRelease() {
		return incrementSnapshotVersionAfterRelease;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.sourcepond.maven.release.config.ParameterRegistration#
	 * setIncrementSnapshotVersionAfterRelease(boolean)
	 */
	@Override
	public void setIncrementSnapshotVersionAfterRelease(final boolean incrementSnapshotVersionAfterRelease) {
		this.incrementSnapshotVersionAfterRelease = incrementSnapshotVersionAfterRelease;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.sourcepond.maven.release.config.Configuration#isSkipTests()
	 */
	@Override
	public boolean isSkipTests() {
		return skipTests;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.sourcepond.maven.release.config.ParameterRegistration#setSkipTests(
	 * boolean)
	 */
	@Override
	public void setSkipTests(final boolean skipTests) {
		this.skipTests = skipTests;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.sourcepond.maven.release.config.Configuration#getUserSettings()
	 */
	@Override
	public File getUserSettings() {
		return userSettings;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.sourcepond.maven.release.config.ParameterRegistration#setUserSettings(
	 * java.io.File)
	 */
	@Override
	public void setUserSettings(final File userSettings) {
		this.userSettings = userSettings;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.sourcepond.maven.release.config.Configuration#getGlobalSettings()
	 */
	@Override
	public File getGlobalSettings() {
		return globalSettings;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.sourcepond.maven.release.config.ParameterRegistration#
	 * setGlobalSettings(java.io.File)
	 */
	@Override
	public void setGlobalSettings(final File globalSettings) {
		this.globalSettings = globalSettings;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.sourcepond.maven.release.config.Configuration#getLocalMavenRepo()
	 */
	@Override
	public File getLocalMavenRepo() {
		return localMavenRepo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.sourcepond.maven.release.config.ParameterRegistration#
	 * setLocalMavenRepo(java.io.File)
	 */
	@Override
	public void setLocalMavenRepo(final File localMavenRepo) {
		this.localMavenRepo = localMavenRepo;
	}
}
