package ch.sourcepond.maven.release.providers;

import ch.sourcepond.maven.release.PluginException;

public interface Initializable {

	void initialize() throws PluginException;
}
