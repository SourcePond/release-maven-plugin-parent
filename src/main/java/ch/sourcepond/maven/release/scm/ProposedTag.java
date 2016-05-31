package ch.sourcepond.maven.release.scm;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;

import ch.sourcepond.maven.release.version.Version;

public interface ProposedTag extends Version {

	String name();

	Ref saveAtHEAD() throws SCMException;

	void tagAndPush(String remoteUrlOrNull) throws SCMException;

	ObjectId getObjectId();
}