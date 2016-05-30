package ch.sourcepond.maven.release.scm;

import ch.sourcepond.maven.release.version.Version;

public interface ProposedTags extends Iterable<ProposedTag> {

	ProposedTag getTag(String tag, Version version) throws SCMException;

	void tagAndPushRepo() throws SCMException;
}
