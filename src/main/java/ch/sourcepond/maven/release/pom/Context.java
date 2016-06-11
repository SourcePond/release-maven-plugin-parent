package ch.sourcepond.maven.release.pom;

import static java.lang.String.format;

import java.util.LinkedList;
import java.util.List;

import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;

import ch.sourcepond.maven.release.reactor.Reactor;
import ch.sourcepond.maven.release.reactor.UnresolvedSnapshotDependencyException;
import ch.sourcepond.maven.release.version.Version;

class Context {

	private final List<String> errors = new LinkedList<>();
	private final Reactor reactor;
	private final Model model;
	private final MavenProject project;
	private final boolean incrementSnapshotVersionAfterRelease;
	private boolean needsOwnVersion;
	private boolean parentUpdated;
	private boolean dependencyUpdated;

	Context(final Reactor reactor, final MavenProject project, final Model model,
			final boolean incrementSnapshotVersionAfterRelease) {
		this.reactor = reactor;
		this.project = project;
		this.model = model;
		this.incrementSnapshotVersionAfterRelease = incrementSnapshotVersionAfterRelease;
	}

	public void addError(final String format, final Object... args) {
		errors.add(format(format, args));
	}

	public Model getModel() {
		return model;
	}

	public MavenProject getProject() {
		return project;
	}

	public List<String> getErrors() {
		return errors;
	}

	public String getVersionToDependOn(final String groupId, final String artifactId)
			throws UnresolvedSnapshotDependencyException {
		final Version version = reactor.find(groupId, artifactId).getVersion();

		String versionToDependOn = null;
		if (incrementSnapshotVersionAfterRelease) {
			versionToDependOn = version.getNextDevelopmentVersion();
		} else if (version.getEquivalentVersionOrNull() == null) {
			versionToDependOn = version.getReleaseVersion();
		} else {
			versionToDependOn = version.getEquivalentVersionOrNull();
		}

		return versionToDependOn;
	}

	public boolean incrementSnapshotVersionAfterRelease() {
		return incrementSnapshotVersionAfterRelease;
	}

	public boolean needsOwnVersion() {
		return needsOwnVersion;
	}

	public void setNeedsOwnVersion(final boolean pNeedsOwnVersion) {
		needsOwnVersion = pNeedsOwnVersion;
	}

	public boolean parentUpdated() {
		return parentUpdated;
	}

	public void setParentUpdated() {
		if (!parentUpdated) {
			parentUpdated = true;
		}
	}

	public boolean dependencyUpdated() {
		return dependencyUpdated;
	}

	public void setDependencyUpdated() {
		if (!dependencyUpdated) {
			dependencyUpdated = true;
		}
	}
}