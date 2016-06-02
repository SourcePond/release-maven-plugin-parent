package ch.sourcepond.maven.release.substitution;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.model.Plugin;

/**
 * @author rolandhauser
 *
 */
@Named("pluginAdapter")
@Singleton
final class PluginAdapter implements PropertyAdapter<Plugin> {

	@Override
	public String getArtifactId(final Plugin origin) {
		return origin.getArtifactId();
	}

	@Override
	public String getGroupId(final Plugin origin) {
		return origin.getGroupId();
	}

	@Override
	public String getVersion(final Plugin origin) {
		return origin.getVersion();
	}

}