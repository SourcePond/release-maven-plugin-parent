package ch.sourcepond.maven.release.substitution;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.model.Dependency;

/**
 * @author rolandhauser
 *
 */
@Named("dependencyAdapter")
@Singleton
class DependencyAdapter implements PropertyAdapter<Dependency> {

	@Override
	public String getArtifactId(final Dependency origin) {
		return origin.getArtifactId();
	}

	@Override
	public String getGroupId(final Dependency origin) {
		return origin.getGroupId();
	}

	@Override
	public String getVersion(final Dependency origin) {
		return origin.getVersion();
	}

}