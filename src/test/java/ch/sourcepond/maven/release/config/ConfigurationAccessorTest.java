package ch.sourcepond.maven.release.config;

import static java.lang.Character.toLowerCase;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.reflect.FieldUtils.getField;
import static org.apache.commons.lang3.reflect.FieldUtils.writeField;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import ch.sourcepond.maven.release.NextMojo;
import ch.sourcepond.maven.release.ReleaseMojo;
import ch.sourcepond.maven.release.config.Configuration;
import ch.sourcepond.maven.release.config.ConfigurationAccessor;
import ch.sourcepond.maven.release.pom.Updater;
import ch.sourcepond.maven.release.providers.MavenComponentSingletons;
import ch.sourcepond.maven.release.providers.RootProject;
import ch.sourcepond.maven.release.reactor.ReactorFactory;
import ch.sourcepond.maven.release.scm.SCMRepository;

public class ConfigurationAccessorTest {
	private static final Long ANY_BUILD_NUMBER = 9l;
	private static final String ANY_SERVER_ID = "anyServerId";
	private static final String ANY_KNOWN_HOSTS = "anyKnownHosts";
	private static final String ANY_IDENTITY_FILE = "anyIdentityFile";
	private static final String ANY_PASSPHRASE = "anyPassphrase";
	private static final File ANY_USER_SETTINGS = new File("anyUserSettings");
	private static final File ANY_GLOBAL_SETTINGS = new File("anyGlobalSettings");
	private static final File ANY_LOCAL_MAVEN_REPO = new File("anyLocalMavenRepo");
	private final List<String> modulesToRelease = asList("mod1", "mod2");
	private final List<String> modulesToToForceRelease = asList("mod3", "mod4");
	private final List<String> goals = asList("goal1", "goal2");
	private final List<String> releaseProfiles = asList("prof1", "prof2");

	private String toCamelCase(final String pName) {
		final String[] tokens = pName.split("_");
		final StringBuilder builder = new StringBuilder(tokens[0].toLowerCase());
		for (int i = 1; i < tokens.length; i++) {
			builder.append(Character.toUpperCase(tokens[i].charAt(0)));
			builder.append(tokens[i].substring(1).toLowerCase());
		}
		return builder.toString();
	}

	@Test
	public void verifyMethodNames() {
		final Set<String> methodNames = new HashSet<>();
		for (final Method m : Configuration.class.getMethods()) {
			String name = m.getName();
			if (name.startsWith("get")) {
				name = name.substring(3);
			} else { // is...
				name = name.substring(2);
			}
			name = name.replace("OrNull", "");
			methodNames.add(toLowerCase(name.charAt(0)) + name.substring(1));
		}

		for (final Field constant : Configuration.class.getFields()) {
			assertTrue(methodNames.remove(toCamelCase(constant.getName())));
		}
	}

	@Test
	public void verifyConstantNames() throws Exception {
		for (final Field constant : Configuration.class.getFields()) {
			assertEquals(toCamelCase(constant.getName()), constant.get(Configuration.class));
		}
	}

	@Test
	public void verifyConstantHasFieldInMojo() throws Exception {
		for (final Field constant : Configuration.class.getFields()) {
			assertNotNull(format("Constant %s refers a field which does not exist on %s", constant, ReleaseMojo.class),
					getField(ReleaseMojo.class, (String) constant.get(Configuration.class), true));
		}
	}

	@Test
	public void verifyWithNextMojo() throws Exception {
		final NextMojo mojo = new NextMojo(mock(SCMRepository.class), mock(ReactorFactory.class),
				mock(MavenComponentSingletons.class), mock(RootProject.class));
		writeField(mojo, Configuration.BUILD_NUMBER, ANY_BUILD_NUMBER, true);
		writeField(mojo, Configuration.MODULES_TO_RELEASE, modulesToRelease, true);
		writeField(mojo, Configuration.MODULES_TO_FORCE_RELEASE, modulesToToForceRelease, true);
		writeField(mojo, Configuration.DISABLE_SSH_AGENT, true, true);
		writeField(mojo, Configuration.DEBUG_ENABLED, true, true);
		writeField(mojo, Configuration.SERVER_ID, ANY_SERVER_ID, true);
		writeField(mojo, Configuration.KNOWN_HOSTS, ANY_KNOWN_HOSTS, true);
		writeField(mojo, Configuration.PRIVATE_KEY, ANY_IDENTITY_FILE, true);
		writeField(mojo, Configuration.PASSPHRASE, ANY_PASSPHRASE, true);

		final ConfigurationAccessor accessor = new ConfigurationAccessor(mojo);
		assertEquals(ANY_BUILD_NUMBER, accessor.getBuildNumberOrNull());
		assertEquals(modulesToRelease, accessor.getModulesToRelease());
		assertEquals(modulesToToForceRelease, accessor.getModulesToForceRelease());
		assertTrue(accessor.isDisableSshAgent());
		assertTrue(accessor.isDebugEnabled());
		assertEquals(ANY_SERVER_ID, accessor.getServerId());
		assertEquals(ANY_KNOWN_HOSTS, accessor.getKnownHosts());
		assertEquals(ANY_IDENTITY_FILE, accessor.getPrivateKey());
		assertEquals(ANY_PASSPHRASE, accessor.getPassphrase());

		assertNull(accessor.getGoals());
		assertNull(accessor.getReleaseProfiles());
		assertFalse(accessor.isIncrementSnapshotVersionAfterRelease());
		assertFalse(accessor.isSkipTests());
		assertNull(accessor.getUserSettings());
		assertNull(accessor.getGlobalSettings());
		assertNull(accessor.getLocalMavenRepo());
	}

	@Test
	public void verifyWithReleaseMojo() throws Exception {
		final ReleaseMojo mojo = new ReleaseMojo(mock(SCMRepository.class), mock(ReactorFactory.class),
				mock(MavenComponentSingletons.class), mock(RootProject.class), mock(Updater.class));

		writeField(mojo, Configuration.BUILD_NUMBER, ANY_BUILD_NUMBER, true);
		writeField(mojo, Configuration.MODULES_TO_RELEASE, modulesToRelease, true);
		writeField(mojo, Configuration.MODULES_TO_FORCE_RELEASE, modulesToToForceRelease, true);
		writeField(mojo, Configuration.DISABLE_SSH_AGENT, true, true);
		writeField(mojo, Configuration.DEBUG_ENABLED, true, true);
		writeField(mojo, Configuration.SERVER_ID, ANY_SERVER_ID, true);
		writeField(mojo, Configuration.KNOWN_HOSTS, ANY_KNOWN_HOSTS, true);
		writeField(mojo, Configuration.PRIVATE_KEY, ANY_IDENTITY_FILE, true);
		writeField(mojo, Configuration.PASSPHRASE, ANY_PASSPHRASE, true);
		writeField(mojo, Configuration.GOALS, goals, true);
		writeField(mojo, Configuration.RELEASE_PROFILES, releaseProfiles, true);
		writeField(mojo, Configuration.INCREMENT_SNAPSHOT_VERSION_AFTER_RELEASE, true, true);
		writeField(mojo, Configuration.SKIP_TESTS, true, true);
		writeField(mojo, Configuration.USER_SETTINGS, ANY_USER_SETTINGS, true);
		writeField(mojo, Configuration.GLOBAL_SETTINGS, ANY_GLOBAL_SETTINGS, true);
		writeField(mojo, Configuration.LOCAL_MAVEN_REPO, ANY_LOCAL_MAVEN_REPO, true);

		final ConfigurationAccessor accessor = new ConfigurationAccessor(mojo);
		assertEquals(ANY_BUILD_NUMBER, accessor.getBuildNumberOrNull());
		assertEquals(modulesToRelease, accessor.getModulesToRelease());
		assertEquals(modulesToToForceRelease, accessor.getModulesToForceRelease());
		assertTrue(accessor.isDisableSshAgent());
		assertTrue(accessor.isDebugEnabled());
		assertEquals(ANY_SERVER_ID, accessor.getServerId());
		assertEquals(ANY_KNOWN_HOSTS, accessor.getKnownHosts());
		assertEquals(ANY_IDENTITY_FILE, accessor.getPrivateKey());
		assertEquals(ANY_PASSPHRASE, accessor.getPassphrase());

		assertEquals(goals, accessor.getGoals());
		assertEquals(releaseProfiles, accessor.getReleaseProfiles());
		assertTrue(accessor.isIncrementSnapshotVersionAfterRelease());
		assertTrue(accessor.isSkipTests());
		assertEquals(ANY_USER_SETTINGS, accessor.getUserSettings());
		assertEquals(ANY_GLOBAL_SETTINGS, accessor.getGlobalSettings());
		assertEquals(ANY_LOCAL_MAVEN_REPO, accessor.getLocalMavenRepo());
	}
}
