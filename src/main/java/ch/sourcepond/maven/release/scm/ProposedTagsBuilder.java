package ch.sourcepond.maven.release.scm;

import ch.sourcepond.maven.release.version.Version;

public interface ProposedTagsBuilder {

	ProposedTagsBuilder add(String tag, Version version) throws SCMException;

	ProposedTags build() throws SCMException;
}
