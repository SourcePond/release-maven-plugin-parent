package ch.sourcepond.maven.release;

import static ch.sourcepond.maven.release.ReleaseInvoker.DEPLOY;
import static ch.sourcepond.maven.release.ReleaseInvoker.DEPLOY_AT_END;
import static ch.sourcepond.maven.release.ReleaseInvoker.SKIP_TESTS;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import ch.sourcepond.maven.release.commons.Version;
import ch.sourcepond.maven.release.providers.RootProject;
import ch.sourcepond.maven.release.reactor.Reactor;
import ch.sourcepond.maven.release.reactor.ReleasableModule;


/**
 * @author Roland Hauser sourcepond@gmail.com
 *
 */
public class ReleaseInvokerTest {
	private final static String ACTIVE_PROFILE_ID = "activeProfile";
	private final static String SOME_PROFILE_ID = "someProfile";
	private final static File GLOBAL_SETTINGS = new File("file:///globalSettings");
	private final static File USER_SETTINGS = new File("file:///globalSettings");
	private final static File LOCAL_REPO = new File(".");
	private final static String MODULE_PATH = "modulePath";
	private final static String SITE = "site";
	private final Log log = mock(Log.class);
	private final RootProject project = mock(RootProject.class);
	private final InvocationRequest request = mock(InvocationRequest.class);
	private final InvocationResult result = mock(InvocationResult.class);
	private final Invoker invoker = mock(Invoker.class);
	private final List<String> goals = new LinkedList<String>();
	private final List<String> modulesToRelease = new LinkedList<String>();
	private final List<String> releaseProfiles = new LinkedList<String>();
	@SuppressWarnings("unchecked")
	private final Iterator<ReleasableModule> modulesInBuildOrder = mock(Iterator.class);
	private final Version version = mock(Version.class);
	private final Reactor reactor = mock(Reactor.class);
	private final ReleasableModule module = mock(ReleasableModule.class);
	private final ReleaseInvoker releaseInvoker = new ReleaseInvoker(log, project, request, invoker);

	@Before
	public void setup() throws Exception {
		when(modulesInBuildOrder.next()).thenReturn(module).thenThrow(NoSuchElementException.class);
		when(modulesInBuildOrder.hasNext()).thenReturn(true).thenReturn(false);
		when(reactor.iterator()).thenReturn(modulesInBuildOrder);
		when(log.isDebugEnabled()).thenReturn(true);
		when(invoker.execute(request)).thenReturn(result);
		when(module.getRelativePathToModule()).thenReturn(MODULE_PATH);
		when(module.getVersion()).thenReturn(version);
	}

	@Test
	public void verifyDefaultConstructor() {
		new ReleaseInvoker(log, project);
	}

	@Test
	public void runMavenBuild_BaseTest() throws Exception {
		releaseInvoker.runMavenBuild(reactor);
		verify(request).setInteractive(false);
		verify(request).setShowErrors(true);
		verify(request).setDebug(true);
		verify(log).isDebugEnabled();
		verify(request, never()).setAlsoMake(Mockito.anyBoolean());
		verify(request).setGoals(Mockito.argThat(new ArgumentMatcher<List<String>>() {

			@Override
			public boolean matches(final List<String> goals) {
				return goals.size() == 1 && goals.contains(DEPLOY);
			}
		}));
		verify(request).setProjects(Mockito.argThat(new ArgumentMatcher<List<String>>() {

			@Override
			public boolean matches(List<String> projects) {
				return projects.isEmpty();
			}
		}));
		verify(log).info("About to run mvn [deploy] with no profiles activated and modules []");
	}

	@Test
	public void runMavenBuild_WithUserSettings() throws Exception {
		releaseInvoker.setUserSettings(USER_SETTINGS);
		releaseInvoker.runMavenBuild(reactor);
		verify(request).setUserSettingsFile(USER_SETTINGS);
	}

	@Test
	public void runMavenBuild_WithGlobalSettings() throws Exception {
		releaseInvoker.setGlobalSettings(GLOBAL_SETTINGS);
		releaseInvoker.runMavenBuild(reactor);
		verify(request).setGlobalSettingsFile(GLOBAL_SETTINGS);
	}

	@Test
	public void runMavenBuild_WithReleasableModule() throws Exception {
		// releaseProfiles.add(e)
	}

	@Test
	public void runMavenBuild_WithGoals() throws Exception {
		goals.add(SITE);
		releaseInvoker.setGoals(goals);
		releaseInvoker.runMavenBuild(reactor);
		verify(request).setGoals(Mockito.argThat(new ArgumentMatcher<List<String>>() {

			@Override
			public boolean matches(final List<String> goals) {
				return goals.size() == 1 && goals.contains(SITE);
			}
		}));
	}

	@Test
	public void runMavenBuild_WithActiveProfiles() throws Exception {
		releaseProfiles.add(SOME_PROFILE_ID);
		releaseInvoker.setReleaseProfiles(releaseProfiles);
		when(project.getActiveProfileIds()).thenReturn(asList(ACTIVE_PROFILE_ID));
		releaseInvoker.runMavenBuild(reactor);
		verify(request).setProfiles(Mockito.argThat(new ArgumentMatcher<List<String>>() {

			@Override
			public boolean matches(final List<String> profiles) {
				return profiles.size() == 2 && profiles.contains(ACTIVE_PROFILE_ID)
						&& profiles.contains(SOME_PROFILE_ID);
			}
		}));
	}

	@Test
	public void runMavenBuild_UserExplicitlyWantsThisToBeReleased() throws Exception {
		modulesToRelease.add(MODULE_PATH);
		releaseInvoker.setModulesToRelease(modulesToRelease);
		releaseInvoker.runMavenBuild(reactor);
		verify(request).setProjects(Mockito.argThat(new ArgumentMatcher<List<String>>() {

			@Override
			public boolean matches(final List<String> modules) {
				return modules.size() == 1 && modules.contains(MODULE_PATH);
			}
		}));
	}

	@Test
	public void runMavenBuild_UserImplicitlyWantsThisToBeReleased() throws Exception {
		when(version.hasChanged()).thenReturn(true);
		releaseInvoker.setModulesToRelease(modulesToRelease);
		releaseInvoker.runMavenBuild(reactor);
		verify(request).setProjects(Mockito.argThat(new ArgumentMatcher<List<String>>() {

			@Override
			public boolean matches(final List<String> modules) {
				return modules.size() == 1 && modules.contains(MODULE_PATH);
			}
		}));
	}

	@Test
	public void runMavenBuild_UserImplicitlyWantsThisToBeReleased_WillNotBeReleased() throws Exception {
		releaseInvoker.setModulesToRelease(modulesToRelease);
		releaseInvoker.runMavenBuild(reactor);
		verify(request).setProjects(Mockito.argThat(new ArgumentMatcher<List<String>>() {

			@Override
			public boolean matches(final List<String> projects) {
				return projects.isEmpty();
			}

			@Override
			public String toString() {
				return "projects";
			}
		}));
	}
	
	@Test
	public void verifyCloneSystemProperties() throws Exception {
		releaseInvoker.runMavenBuild(reactor);
		verify(request).setProperties(Mockito.argThat(new ArgumentMatcher<Properties>() {

			@Override
			public boolean matches(final Properties env) {
				for (final String key : ReleaseInvoker.DEFAULT_VM_PROPERTY_NAMES) {
					if (env.contains(key)) {
						return false;
					}
				}
				return true;
			}
		}));
	}


	@Test
	public void releaseAtEnd() throws Exception {
		releaseInvoker.runMavenBuild(reactor);
		verify(request).setProperties(Mockito.argThat(new ArgumentMatcher<Properties>() {

			@Override
			public boolean matches(final Properties env) {
				return env.containsKey(DEPLOY_AT_END);
			}
		}));
	}
	
	@Test
	public void skipTests() throws Exception {
		releaseInvoker.setSkipTests(true);
		releaseInvoker.runMavenBuild(reactor);
		verify(request).setProperties(Mockito.argThat(new ArgumentMatcher<Properties>() {

			@Override
			public boolean matches(final Properties env) {
				return env.containsKey(SKIP_TESTS);
			}
		}));
		verify(request).setGoals(Mockito.argThat(new ArgumentMatcher<List<String>>() {

			@Override
			public boolean matches(final List<String> goals) {
				return goals.size() == 1 && goals.contains(DEPLOY);
			}
		}));
	}

	@Test
	public void setLocalMavenRepo() throws Exception {
		releaseInvoker.setLocalMavenRepo(LOCAL_REPO);
		verify(request).setLocalRepositoryDirectory(LOCAL_REPO.getCanonicalFile());
	}

	@Test(expected = MojoExecutionException.class)
	public void runMavenBuild_ErrorExitCode() throws Exception {
		when(result.getExitCode()).thenReturn(1);
		releaseInvoker.runMavenBuild(reactor);
	}

	@Test
	public void runMavenBuild_InvocationFailed() throws Exception {
		final MavenInvocationException expected = new MavenInvocationException("anyMessage");
		doThrow(expected).when(invoker).execute(request);
		try {
			releaseInvoker.runMavenBuild(reactor);
			fail("Exception expected here");
		} catch (final MojoExecutionException e) {
			assertSame(expected, e.getCause());
		}
	}
}
