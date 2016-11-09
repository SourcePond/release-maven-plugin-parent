package ch.sourcepond.maven.release.scm.spi;

import org.apache.maven.plugin.logging.Log;

import ch.sourcepond.maven.release.scm.SCMRepository;

public interface SCMBuilder {

	SCMBuilder setRemoteRepositoryEnabled(boolean pRemoteRepositoryEnabled);
	
	SCMBuilder setRemotePushEnabled(boolean pRemotePushEnabled);
	
	SCMBuilder setRemoteUrl(String pRemoteUrlOrNull);
	
	SCMBuilder setIncrementSnapshotVersionAfterRelease(boolean pIsIncrementSnapshotVersionAfterRelease);
	
	SCMBuilder setLog(Log log);
	
	SCMRepository build();
}
