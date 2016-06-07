package ch.sourcepond.maven.release.version;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Default implementation of the {@link VersionBuilderFactory} interface.
 *
 */
@Named
@Singleton
final class DefaultVersionFactory implements VersionBuilderFactory {
	static final String SNAPSHOT_EXTENSION = "-SNAPSHOT";
	private BuildNumberFinder finder;
	private ChangeDetectorFactory detectorFactory;

	@Inject
	DefaultVersionFactory(final BuildNumberFinder pFinder, final ChangeDetectorFactory pDetectorFactory) {
		finder = pFinder;
		detectorFactory = pDetectorFactory;
	}

	void setFinder(final BuildNumberFinder finder) {
		this.finder = finder;
	}

	void setDetectorFactory(final ChangeDetectorFactory detectorFactory) {
		this.detectorFactory = detectorFactory;
	}

	@Override
	public VersionBuilder newBuilder() {
		return new DefaultVersionBuilder(finder, detectorFactory);
	}
}
