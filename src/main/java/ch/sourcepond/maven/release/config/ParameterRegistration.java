package ch.sourcepond.maven.release.config;

import java.io.File;
import java.util.List;

import org.apache.maven.settings.Settings;

public interface ParameterRegistration {

	void setBuildNumber(Long buildNumber);

	void setModulesToRelease(List<String> modulesToRelease);

	void setModulesToForceRelease(List<String> modulesToForceRelease);

	void setDisableSshAgent(boolean disableSshAgent);

	void setDebugEnabled(boolean debugEnabled);

	void setSettings(Settings settings);

	void setServerId(String serverId);

	void setKnownHosts(String knownHosts);

	void setPrivateKey(String privateKey);

	void setPassphrase(String passphrase);

	void setGoals(List<String> goals);

	void setReleaseProfiles(List<String> releaseProfiles);

	void setIncrementSnapshotVersionAfterRelease(boolean incrementSnapshotVersionAfterRelease);

	void setSkipTests(boolean skipTests);

	void setUserSettings(File userSettings);

	void setGlobalSettings(File globalSettings);

	void setLocalMavenRepo(File localMavenRepo);

}