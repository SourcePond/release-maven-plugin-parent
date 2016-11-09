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
