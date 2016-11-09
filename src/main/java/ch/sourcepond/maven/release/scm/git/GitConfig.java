package ch.sourcepond.maven.release.scm.git;

import org.apache.maven.plugin.logging.Log;

import ch.sourcepond.maven.release.scm.SCMRepository;
import ch.sourcepond.maven.release.scm.spi.SCMBuilder;

//TODO: Make this class package private when SingleModuleTest is working with a Guice injector
public class GitConfig implements SCMBuilder {
	private boolean remoteRepositoryEnabled;
	private boolean remotePushEnabled;
	private String remoteUrlOrNull;
	private boolean incrementSnapshotVersionAfterRelease;
	private Log log;

	@Override
	public SCMBuilder setRemoteRepositoryEnabled(boolean pRemoteRepositoryEnabled) {
		remoteRepositoryEnabled = pRemoteRepositoryEnabled;
		return this;
	}

	@Override
	public SCMBuilder setRemotePushEnabled(boolean pRemotePushEnabled) {
		remotePushEnabled = pRemotePushEnabled;
		return this;
	}

	@Override
	public SCMBuilder setRemoteUrl(String pRemoteUrlOrNull) {
		remoteUrlOrNull = pRemoteUrlOrNull;
		return this;
	}

	@Override
	public SCMBuilder setIncrementSnapshotVersionAfterRelease(boolean pIsIncrementSnapshotVersionAfterRelease) {
		incrementSnapshotVersionAfterRelease = pIsIncrementSnapshotVersionAfterRelease;
		return this;
	}

	@Override
	public SCMRepository build() {
		return new GitRepository(log, this);
	}

	 
	boolean isIncrementSnapshotVersionAfterRelease() {
		return incrementSnapshotVersionAfterRelease;
	}
	
	boolean isRemotePushEnabled() {
		return remotePushEnabled;
	}
	
	boolean isRemoteRepositoryEnabled() {
		return remoteRepositoryEnabled;
	}
	
	String getRemoteUrlOrNull() {
		return remoteUrlOrNull;
	}

	@Override
	public SCMBuilder setLog(final Log pLog) {
		log = pLog;
		return this;
	}

}
