/*Copyright (C) 2016 Roland Hauser, <sourcepond@gmail.com>
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package ch.sourcepond.maven.release.scm.git;

import static ch.sourcepond.maven.release.scm.git.GitProposedTag.BUILD_NUMBER;
import static ch.sourcepond.maven.release.scm.git.GitProposedTag.VERSION;
import static java.lang.String.format;
import static org.apache.commons.lang3.Validate.notNull;
import static org.eclipse.jgit.lib.Repository.isValidRefName;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.logging.Log;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import ch.sourcepond.maven.release.scm.ProposedTag;
import ch.sourcepond.maven.release.scm.ProposedTagsBuilder;
import ch.sourcepond.maven.release.scm.SCMException;
import ch.sourcepond.maven.release.scm.SCMRepository;

//TODO: Make this class package private when SingleModuleTest is working with a Guice injector
public class GitRepository implements SCMRepository {
	private static final String REFS_TAGS = "refs/tags/";
	static final String SNAPSHOT_COMMIT_MESSAGE = "Incremented SNAPSHOT-version for next development iteration";
	static final String INVALID_REF_NAME_MESSAGE = "Sorry, '%s' is not a valid version.";
	private final Log log;
	private final GitConfig config;
	private Git git;
	private SCMException gitInstantiationException;
	private Collection<Ref> remoteTags;

	public GitRepository(final Log pLog, final GitConfig pConfig) {
		log = pLog;
		config = pConfig;
	}

	public Git newGit() throws SCMException {
		final File gitDir = new File(".");
		try {
			return Git.open(gitDir);
		} catch (final RepositoryNotFoundException rnfe) {
			final String fullPathOfCurrentDir = pathOf(gitDir);
			final File gitRoot = getGitRootIfItExistsInOneOfTheParentDirectories(new File(fullPathOfCurrentDir));
			if (gitRoot == null) {
				throw new SCMException("Releases can only be performed from Git repositories.")
						.add("%s is not a Git repository.", fullPathOfCurrentDir);
			}
			throw new SCMException("The release plugin can only be run from the root folder of your Git repository")
					.add("%s is not the root of a Gir repository", fullPathOfCurrentDir)
					.add("Try running the release plugin from %s", pathOf(gitRoot));
		} catch (final Exception e) {
			throw new SCMException("Could not open git repository. Is %s a git repository?", pathOf(gitDir))
					.add("Exception returned when accessing the git repo: %s", e.toString());
		}
	}

	private static String pathOf(final File file) {
		String path;
		try {
			path = file.getCanonicalPath();
		} catch (final IOException e1) {
			path = file.getAbsolutePath();
		}
		return path;
	}

	private static File getGitRootIfItExistsInOneOfTheParentDirectories(File candidateDir) {
		while (candidateDir != null && /* HACK ATTACK! Maybe.... */ !candidateDir.getName().equals("target")) {
			if (new File(candidateDir, ".git").isDirectory()) {
				return candidateDir;
			}
			candidateDir = candidateDir.getParentFile();
		}
		return null;
	}

	private Git getGit() throws SCMException {
		if (git == null && gitInstantiationException == null) {
			try {
				git = newGit();
			} catch (final SCMException e) {
				gitInstantiationException = e;
			}
		}

		if (git == null) {
			throw gitInstantiationException;
		}

		return git;
	}

	@Override
	public Collection<Long> getRemoteBuildNumbers(final String artifactId, final String versionWithoutBuildNumber)
			throws SCMException {
		final Collection<Ref> remoteTagRefs = allRemoteTags();
		final Collection<Long> remoteBuildNumbers = new ArrayList<>();
		final String tagWithoutBuildNumber = artifactId + "-" + versionWithoutBuildNumber;
		for (final Ref remoteTagRef : remoteTagRefs) {
			final String remoteTagName = remoteTagRef.getName();
			final Long buildNumber = buildNumberOf(tagWithoutBuildNumber, remoteTagName);
			if (buildNumber != null) {
				remoteBuildNumbers.add(buildNumber);
			}
		}
		return remoteBuildNumbers;
	}

	public Collection<Ref> allRemoteTags() throws SCMException {
		if (remoteTags == null) {
			final LsRemoteCommand lsRemoteCommand = getGit().lsRemote().setTags(true).setHeads(false);
			if (config.getRemoteUrlOrNull() != null) {
				lsRemoteCommand.setRemote(config.getRemoteUrlOrNull());
			}
			try {
				remoteTags = lsRemoteCommand.call();
			} catch (final GitAPIException e) {
				throw new SCMException(e, "Remote tags could not be listed!");
			}
		}
		return remoteTags;
	}

	@Override
	public boolean hasLocalTag(final String tagName) throws SCMException {
		try {
			for (final Ref ref : getGit().tagList().call()) {
				final String currentTag = ref.getName().replace(REFS_TAGS, "");
				if (tagName.equals(currentTag)) {
					return true;
				}
			}
			return false;
		} catch (final GitAPIException e) {
			throw new SCMException(e, "Local tag could not be determined!");
		}
	}

	private Status currentStatus() throws SCMException {
		Status status;
		try {
			status = getGit().status().call();
		} catch (final GitAPIException e) {
			throw new SCMException(e, "Error while checking if the Git repo is clean");
		}
		return status;
	}

	@Override
	public void errorIfNotClean() throws SCMException {
		final Status status = currentStatus();
		final boolean isClean = status.isClean();
		if (!isClean) {
			final SCMException exception = new SCMException(
					"Cannot release with uncommitted changes. Please check the following files:");
			final Set<String> uncommittedChanges = status.getUncommittedChanges();
			if (uncommittedChanges.size() > 0) {
				exception.add("Uncommitted:");
				for (final String path : uncommittedChanges) {
					exception.add(" * %s", path);
				}
			}
			final Set<String> untracked = status.getUntracked();
			if (untracked.size() > 0) {
				exception.add("Untracked:");
				for (final String path : untracked) {
					exception.add(" * %s", path);
				}
			}
			throw exception.add("Please commit or revert these changes before releasing.");
		}
	}

	@Override
	public void revertChanges(final Collection<File> changedFiles) throws SCMException {
		try {
			final File workTree = getGit().getRepository().getWorkTree().getCanonicalFile();
			final SCMException exception = new SCMException("Reverting changed POMs failed!");

			for (final File changedFile : changedFiles) {
				try {
					final String pathRelativeToWorkingTree = Repository.stripWorkDir(workTree, changedFile);
					getGit().checkout().addPath(pathRelativeToWorkingTree).call();
				} catch (final Exception e) {
					exception.add(
							" * Unable to revert changes to %s - you may need to manually revert this file. Error was: %s",
							changedFile, e.getMessage());
				}
			}

			if (!exception.getMessages().isEmpty()) {
				throw exception;
			}
		} catch (NoWorkTreeException | IOException e) {
			throw new SCMException(e, "Working directory could not be determined!");
		}
	}

	@Override
	public List<ProposedTag> tagsForVersion(final String module, final String versionWithoutBuildNumber)
			throws SCMException {
		final List<ProposedTag> results = new ArrayList<>();
		List<Ref> tags;
		try {
			tags = getGit().tagList().call();
		} catch (final GitAPIException e) {
			throw new SCMException(e, "Error while getting a list of tags in the local repo");
		}
		Collections.reverse(tags);
		final String tagWithoutBuildNumber = module + "-" + versionWithoutBuildNumber;
		for (final Ref tag : tags) {
			if (isPotentiallySameVersionIgnoringBuildNumber(tagWithoutBuildNumber, tag.getName())) {
				results.add(fromRef(tag));
			}
		}
		return results;

	}

	static boolean isPotentiallySameVersionIgnoringBuildNumber(final String versionWithoutBuildNumber,
			final String refName) {
		return buildNumberOf(versionWithoutBuildNumber, refName) != null;
	}

	public static Long buildNumberOf(final String versionWithoutBuildNumber, final String refName) {
		final String tagName = stripRefPrefix(refName);
		final String prefix = versionWithoutBuildNumber + ".";
		if (tagName.startsWith(prefix)) {
			final String end = tagName.substring(prefix.length());
			try {
				return Long.parseLong(end);
			} catch (final NumberFormatException e) {
				return null;
			}
		}
		return null;
	}

	@Override
	public ProposedTag fromRef(final Ref gitTag) throws SCMException {
		notNull(gitTag, "gitTag");

		final RevWalk walk = new RevWalk(getGit().getRepository());
		final ObjectId tagId = gitTag.getObjectId();
		JSONObject message;
		try {
			final RevTag tag = walk.parseTag(tagId);
			message = (JSONObject) JSONValue.parse(tag.getFullMessage());
		} catch (final IOException e) {
			throw new SCMException(e, "Error while looking up tag because RevTag could not be parsed! Object-id was %s",
					tagId);
		} finally {
			walk.dispose();
		}
		if (message == null) {
			message = new JSONObject();
			message.put(VERSION, "0");
			message.put(BUILD_NUMBER, "0");
		}
		return new GitProposedTag(getGit(), log, gitTag, stripRefPrefix(gitTag.getName()), message,
				config.getRemoteUrlOrNull());
	}

	static String stripRefPrefix(final String refName) {
		return refName.substring(REFS_TAGS.length());
	}

	@Override
	public ProposedTagsBuilder newProposedTagsBuilder() throws SCMException {
		return new GitProposedTagsBuilder(log, getGit(), this, config.getRemoteUrlOrNull());
	}

	@Override
	public void checkValidRefName(final String releaseVersion) throws SCMException {
		if (!isValidRefName(format("%s%s", REFS_TAGS, releaseVersion))) {
			throw new SCMException(INVALID_REF_NAME_MESSAGE, releaseVersion)
					.add("Version numbers are used in the Git tag, and so can only contain characters that are valid in git tags.")
					.add("Please see https://www.kernel.org/pub/software/scm/git/docs/git-check-ref-format.html for tag naming rules.");
		}
	}

	@Override
	public boolean hasChangedSince(final String modulePath, final List<String> childModules,
			final Collection<ProposedTag> tags) throws SCMException {
		final RevWalk walk = new RevWalk(getGit().getRepository());
		try {
			walk.setRetainBody(false);
			walk.markStart(walk.parseCommit(getGit().getRepository().findRef("HEAD").getObjectId()));
			filterOutOtherModulesChanges(modulePath, childModules, walk);
			stopWalkingWhenTheTagsAreHit(tags, walk);

			final Iterator<RevCommit> it = walk.iterator();
			boolean changed = it.hasNext();

			if (config.isIncrementSnapshotVersionAfterRelease() && changed) {
				final RevCommit commit = it.next();
				walk.parseBody(commit);
				changed = !SNAPSHOT_COMMIT_MESSAGE.equals(commit.getShortMessage()) || it.hasNext();
			}

			return changed;
		} catch (final IOException e) {
			throw new SCMException(e, "Diff detector could not determine whether module %s has been changed!",
					modulePath);
		} finally {
			walk.dispose();
		}
	}

	private static void stopWalkingWhenTheTagsAreHit(final Collection<ProposedTag> tags, final RevWalk walk)
			throws IOException {
		for (final ProposedTag tag : tags) {
			final ObjectId commitId = tag.getObjectId();
			final RevCommit revCommit = walk.parseCommit(commitId);
			walk.markUninteresting(revCommit);
		}
	}

	private void filterOutOtherModulesChanges(final String modulePath, final List<String> childModules,
			final RevWalk walk) {
		final boolean isRootModule = ".".equals(modulePath);
		final boolean isMultiModuleProject = !isRootModule || !childModules.isEmpty();
		final List<TreeFilter> treeFilters = new LinkedList<>();
		treeFilters.add(TreeFilter.ANY_DIFF);
		if (isMultiModuleProject) {
			if (!isRootModule) {
				// for sub-modules, look for changes only in the sub-module
				// path...
				treeFilters.add(PathFilter.create(modulePath));
			}

			// ... but ignore any sub-modules of the current sub-module, because
			// they can change independently of the current module
			for (final String childModule : childModules) {
				final String path = isRootModule ? childModule : modulePath + "/" + childModule;
				treeFilters.add(PathFilter.create(path).negate());
			}

		}
		final TreeFilter treeFilter = treeFilters.size() == 1 ? treeFilters.get(0) : AndTreeFilter.create(treeFilters);
		walk.setTreeFilter(treeFilter);
	}

	@Override
	public void pushChanges(final Collection<File> changedFiles) throws SCMException {
		try {
			final File workTree = getGit().getRepository().getWorkTree().getCanonicalFile();
			for (final File changedFile : changedFiles) {
				final String pathRelativeToWorkingTree = Repository.stripWorkDir(workTree, changedFile);
				getGit().add().setUpdate(true).addFilepattern(pathRelativeToWorkingTree).call();
			}
			getGit().commit().setMessage(SNAPSHOT_COMMIT_MESSAGE).call();
			if (config.getRemoteUrlOrNull() != null) {
				getGit().push().setRemote(config.getRemoteUrlOrNull()).call();
			}
		} catch (final GitAPIException | IOException e) {
			throw new SCMException(e, "Changed POM files could not be committed and pushed!");
		}
	}
}
