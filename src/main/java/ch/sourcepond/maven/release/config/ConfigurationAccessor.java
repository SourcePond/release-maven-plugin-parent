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

import static org.apache.commons.lang3.Validate.isTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.maven.plugin.Mojo;

public final class ConfigurationAccessor implements Configuration {
	private final Map<String, Field> fields = new HashMap<>();
	private final Mojo mojo;

	public ConfigurationAccessor(final Mojo pMojo) {
		mojo = pMojo;
	}

	private Field getFieldOrNull(final String pName, final Class<?> pExpectedReturnType) {
		Field field = fields.get(pName);
		if (field == null) {
			field = FieldUtils.getField(mojo.getClass(), pName, true);
			if (field != null) {
				isTrue(field.getType().equals(pExpectedReturnType), "Field %s is not compatible with return type %s",
						field, pExpectedReturnType);
				fields.put(pName, field);
			}
		}
		return field;
	}

	@SuppressWarnings("unchecked")
	private <T> T get(final String pFieldName, final Class<?> pExpectedReturnType) {
		try {
			final Field field = getFieldOrNull(pFieldName, pExpectedReturnType);
			if (field == null) {
				return null;
			}
			return (T) getFieldOrNull(pFieldName, pExpectedReturnType).get(mojo);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	private boolean getBoolean(final String pFieldName) {
		try {
			final Field fieldOrNull = getFieldOrNull(pFieldName, boolean.class);
			return fieldOrNull == null ? false : (boolean) fieldOrNull.get(mojo);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	@Override
	public Long getBuildNumberOrNull() {
		return get(BUILD_NUMBER, Long.class);
	}

	@Override
	public List<String> getModulesToRelease() {
		return get(MODULES_TO_RELEASE, List.class);
	}

	@Override
	public List<String> getModulesToForceRelease() {
		return get(MODULES_TO_FORCE_RELEASE, List.class);
	}

	@Override
	public boolean isDisableSshAgent() {
		return getBoolean(DISABLE_SSH_AGENT);
	}

	@Override
	public boolean isDebugEnabled() {
		return getBoolean(DEBUG_ENABLED);
	}

	@Override
	public String getServerId() {
		return get(SERVER_ID, String.class);
	}

	@Override
	public String getKnownHosts() {
		return get(KNOWN_HOSTS, String.class);
	}

	@Override
	public String getPrivateKey() {
		return get(PRIVATE_KEY, String.class);
	}

	@Override
	public String getPassphrase() {
		return get(PASSPHRASE, String.class);
	}

	@Override
	public List<String> getGoals() {
		return get(GOALS, List.class);
	}

	@Override
	public List<String> getReleaseProfiles() {
		return get(RELEASE_PROFILES, List.class);
	}

	@Override
	public boolean isIncrementSnapshotVersionAfterRelease() {
		return getBoolean(INCREMENT_SNAPSHOT_VERSION_AFTER_RELEASE);
	}

	@Override
	public boolean isSkipTests() {
		return getBoolean(SKIP_TESTS);
	}

	@Override
	public File getUserSettings() {
		return get(USER_SETTINGS, File.class);
	}

	@Override
	public File getGlobalSettings() {
		return get(GLOBAL_SETTINGS, File.class);
	}

	@Override
	public File getLocalMavenRepo() {
		return get(LOCAL_MAVEN_REPO, File.class);
	}

	@Override
	public boolean isRemoteRepositoryEnabled() {
		return getBoolean(REMOTE_REPOSITORY_ENABLED);
	}

	@Override
	public boolean isRemotePushEnabled() {
		return getBoolean(REMOTE_PUSH_ENABLED);
	}
}
