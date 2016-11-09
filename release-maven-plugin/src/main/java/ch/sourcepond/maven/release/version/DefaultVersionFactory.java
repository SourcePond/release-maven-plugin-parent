/*Copyright (C) 2016 Roland Hauser, <sourcepond@gmail.com>
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package ch.sourcepond.maven.release.version;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import ch.sourcepond.maven.release.config.Configuration;

/**
 * Default implementation of the {@link VersionBuilderFactory} interface.
 *
 */
@Named
@Singleton
final class DefaultVersionFactory implements VersionBuilderFactory {
	static final String SNAPSHOT_EXTENSION = "-SNAPSHOT";
	private BuildNumberFinder finder;
	private final Configuration configuration;
	private ChangeDetectorFactory detectorFactory;

	@Inject
	DefaultVersionFactory(final BuildNumberFinder pFinder, final Configuration pConfiguration,
			final ChangeDetectorFactory pDetectorFactory) {
		finder = pFinder;
		configuration = pConfiguration;
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
		return new DefaultVersionBuilder(finder, configuration, detectorFactory);
	}
}
