package ch.sourcepond.maven.release.scm.git;

import javax.inject.Named;
import javax.inject.Singleton;

import ch.sourcepond.maven.release.scm.spi.SCMBuilder;
import ch.sourcepond.maven.release.scm.spi.SCMBuilderFactory;

@Named
@Singleton
final class GitSCMBuilderFactory implements SCMBuilderFactory {
	
	@Override
	public SCMBuilder newBuilder() {
		return new GitConfig();
	}

}
