package ch.sourcepond.maven.release.config;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import ch.sourcepond.maven.release.providers.BaseProvider;
import ch.sourcepond.maven.release.providers.MavenComponentSingletons;

@Named
@Singleton
final class ConfigurationProvider extends BaseProvider<Configuration> {

	@Inject
	ConfigurationProvider(final MavenComponentSingletons pSingletons) {
		super(pSingletons);
	}

	@Override
	protected Configuration getDelegate() {
		return singletons.getConfiguration();
	}

	@Override
	protected Class<Configuration> getDelegateType() {
		return Configuration.class;
	}
}
