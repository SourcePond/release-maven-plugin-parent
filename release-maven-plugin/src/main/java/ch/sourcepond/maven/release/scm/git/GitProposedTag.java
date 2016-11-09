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

import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.json.simple.JSONObject;

import ch.sourcepond.maven.release.commons.BaseVersion;
import ch.sourcepond.maven.release.scm.ProposedTag;
import ch.sourcepond.maven.release.scm.SCMException;


class GitProposedTag extends BaseVersion implements ProposedTag {
	public static final String VERSION = "version";
	public static final String BUILD_NUMBER = "buildNumber";
	private final Log log;
	private final String name;
	private final JSONObject message;
	private final Git git;
	private final String remoteUrlOrNull;
	private Ref ref;

	GitProposedTag(final Git git, final Log log, final Ref ref, final String name, final JSONObject message,
			final String pRemoteUrlOrNull) {
		notBlank(name, "tag name");
		notNull(message, "tag message");
		this.log = log;
		this.git = git;
		this.ref = ref;
		this.name = name;
		this.message = message;
		this.remoteUrlOrNull = pRemoteUrlOrNull;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public Ref saveAtHEAD() throws SCMException {
		final String json = message.toJSONString();
		try {
			ref = git.tag().setName(name()).setAnnotated(true).setMessage(json).call();
		} catch (final GitAPIException e) {
			throw new SCMException(e, "Ref '%s' could be saved at HEAD!", name());
		}
		return ref;
	}

	private void pushAndLogResult(final PushCommand pushCommand)
			throws GitAPIException, InvalidRemoteException, TransportException {
		for (final PushResult result : pushCommand.call()) {
			for (final RemoteRefUpdate upd : result.getRemoteUpdates()) {
				log.info(upd.toString());
			}
		}
	}

	@Override
	public void tagAndPush() throws SCMException {
		log.info(String.format("About to tag the repository with %s", name()));
		try {
			final PushCommand pushCommand = git.push().add(saveAtHEAD());
			if (remoteUrlOrNull != null) {
				pushCommand.setRemote(remoteUrlOrNull);
			}
			pushAndLogResult(pushCommand);
		} catch (final GitAPIException e) {
			throw new SCMException(e, "Tag '%s' could not be pushed!", name());
		}
	}

	@Override
	public String toString() {
		return "AnnotatedTag{" + "name='" + name + '\'' + ", version=" + getBusinessVersion() + ", buildNumber="
				+ getBuildNumber() + '}';
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		final GitProposedTag that = (GitProposedTag) o;
		return name.equals(that.name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public ObjectId getObjectId() {
		return ref.getTarget().getObjectId();
	}

	@Override
	public String getReleaseVersion() {
		return getBusinessVersion() + "." + getBuildNumber();
	}

	@Override
	public String getBusinessVersion() {
		return String.valueOf(message.get(VERSION));
	}

	@Override
	public long getBuildNumber() {
		return Long.parseLong(String.valueOf(message.get(BUILD_NUMBER)));
	}

	@Override
	public String getEquivalentVersionOrNull() {
		return null;
	}

	@Override
	public void makeReleaseable() {
		// noop
	}

	@Override
	public boolean hasChanged() {
		// Always false for a tag
		return false;
	}

	@Override
	public void delete() throws SCMException {
		try {
			final List<String> deleted = git.tagDelete().setTags(name()).call();
			if (!deleted.isEmpty()) {
				final PushCommand pushCommand = git.push().add(":refs/tags/" + name());
				if (remoteUrlOrNull != null) {
					pushCommand.setRemote(remoteUrlOrNull);
				}
				pushAndLogResult(pushCommand);
			}
			log.info(String.format("Deleted tag '%s' from repository", name()));
		} catch (final GitAPIException e) {
			throw new SCMException(e, "Remote tag '%s' could not be deleted!", name());
		}
	}
}