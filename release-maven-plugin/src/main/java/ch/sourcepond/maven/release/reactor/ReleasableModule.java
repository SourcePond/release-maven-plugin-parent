package ch.sourcepond.maven.release.reactor;

import java.util.List;

import org.apache.maven.project.MavenProject;

import ch.sourcepond.maven.release.commons.Version;



public class ReleasableModule {
	private final MavenProject project;
	private final Version version;
	private final String tagName;
	private final String relativePathToModule;

	public ReleasableModule(final MavenProject project, final Version version, final String relativePathToModule) {
		this.project = project;
		this.version = version;
		this.relativePathToModule = relativePathToModule;
		this.tagName = project.getArtifactId() + "-" + version.getReleaseVersion();
	}

	public String getTagName() {
		return tagName;
	}

	public String getArtifactId() {
		return project.getArtifactId();
	}

	public String getGroupId() {
		return project.getGroupId();
	}

	public MavenProject getProject() {
		return project;
	}

	public Version getVersion() {
		return version;
	}

	public boolean isOneOf(final List<String> moduleNames) {
		final String modulePath = project.getBasedir().getName();
		for (final String moduleName : moduleNames) {
			if (modulePath.equals(moduleName)) {
				return true;
			}
		}
		return false;
	}

	public String getRelativePathToModule() {
		return relativePathToModule;
	}
}