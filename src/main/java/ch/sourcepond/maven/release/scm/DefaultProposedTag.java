package ch.sourcepond.maven.release.scm;

import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

import org.apache.maven.plugin.logging.Log;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.json.simple.JSONObject;

import ch.sourcepond.maven.release.version.BaseVersion;

class DefaultProposedTag extends BaseVersion implements ProposedTag {
	public static final String VERSION = "version";
	public static final String BUILD_NUMBER = "buildNumber";
	private final Log log;
	private final String name;
	private final JSONObject message;
	private final Git git;
	private Ref ref;

	DefaultProposedTag(final Git git, final Log log, final Ref ref, final String name, final JSONObject message) {
		notBlank(name, "tag name");
		notNull(message, "tag message");
		this.log = log;
		this.git = git;
		this.ref = ref;
		this.name = name;
		this.message = message;
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
			throw new SCMException(e, "Ref could be saved at HEAD!");
		}
		return ref;
	}

	@Override
	public void tagAndPush(final String remoteUrlOrNull) throws SCMException {
		log.info(String.format("About to tag the repository with %s", name()));
		try {
			final PushCommand pushCommand = git.push().add(saveAtHEAD());
			if (remoteUrlOrNull != null) {
				pushCommand.setRemote(remoteUrlOrNull);
			}
			pushCommand.call();
		} catch (final GitAPIException e) {
			throw new SCMException(e, "Repository could be tagged with %s", name());
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
		final DefaultProposedTag that = (DefaultProposedTag) o;
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
}