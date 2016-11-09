package ch.sourcepond.maven.release.scm;

import java.io.File;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.plugin.logging.Log;
import org.eclipse.jgit.lib.Ref;

import ch.sourcepond.maven.release.config.Configuration;
import ch.sourcepond.maven.release.providers.RootProject;
import ch.sourcepond.maven.release.scm.spi.SCMBuilder;
import ch.sourcepond.maven.release.scm.spi.SCMBuilderFactory;

@Named
@Singleton
final class SCMRepositoryDelegator implements SCMRepository {
	private final SCMBuilderFactory factory;
	private final Configuration config;
	private final RootProject project;
	private final Log log;
	private SCMRepository delegate;

	@Inject
	SCMRepositoryDelegator(final SCMBuilderFactory pScmRepositoryFactory, final Configuration pConfig,
			final RootProject pProject, final Log pLog) {
		factory = pScmRepositoryFactory;
		config = pConfig;
		project = pProject;
		log = pLog;
	}

	private SCMRepository getDelegate() {
		if (delegate == null) {
			final SCMBuilder builder = factory.newBuilder();
			builder.setLog(log);
			builder.setIncrementSnapshotVersionAfterRelease(config.isIncrementSnapshotVersionAfterRelease());
			builder.setRemotePushEnabled(config.isRemotePushEnabled());
			builder.setRemoteRepositoryEnabled(config.isRemoteRepositoryEnabled());
			builder.setRemoteUrl(project.getRemoteUrlOrNull());
			delegate = builder.build();
		}
		return delegate;
	}

	@Override
	public ProposedTag fromRef(final Ref gitTag) throws SCMException {
		return getDelegate().fromRef(gitTag);
	}

	@Override
	public void errorIfNotClean() throws SCMException {
		getDelegate().errorIfNotClean();
	}

	@Override
	public boolean hasLocalTag(final String tag) throws SCMException {
		return getDelegate().hasLocalTag(tag);
	}

	@Override
	public void revertChanges(final Collection<File> changedFiles) throws SCMException {
		getDelegate().revertChanges(changedFiles);
	}

	@Override
	public Collection<Long> getRemoteBuildNumbers(final String artifactId, final String versionWithoutBuildNumber)
			throws SCMException {
		return getDelegate().getRemoteBuildNumbers(artifactId, versionWithoutBuildNumber);
	}

	@Override
	public Collection<ProposedTag> tagsForVersion(final String module, final String versionWithoutBuildNumber)
			throws SCMException {
		return getDelegate().tagsForVersion(module, versionWithoutBuildNumber);
	}

	@Override
	public ProposedTagsBuilder newProposedTagsBuilder() throws SCMException {
		return getDelegate().newProposedTagsBuilder();
	}

	@Override
	public void checkValidRefName(final String releaseVersion) throws SCMException {
		getDelegate().checkValidRefName(releaseVersion);
	}

	@Override
	public boolean hasChangedSince(final String modulePath, final List<String> childModules,
			final Collection<ProposedTag> tags) throws SCMException {
		return getDelegate().hasChangedSince(modulePath, childModules, tags);
	}

	@Override
	public void pushChanges(final Collection<File> changedFiles) throws SCMException {
		getDelegate().pushChanges(changedFiles);
	}

}
