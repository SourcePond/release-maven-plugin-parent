package ch.sourcepond.maven.release.providers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.maven.model.Scm;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;

import ch.sourcepond.maven.release.commons.PluginException;



public class DefaultRootProjectTest {
	private static final String DEVELOPER_CONNECTION = "scm:git:ssh://some/developerPath";
	private static final String CONNECTION = "scm:git:ssh://some/commonPath";
	private final Scm scm = mock(Scm.class);
	private final MavenProject mavenProject = mock(MavenProject.class);
	private final MavenComponentSingletons singletons = mock(MavenComponentSingletons.class);
	private final DefaultRootProject rootProject = new DefaultRootProject(singletons);

	@Before
	public void setup() {
		when(mavenProject.getScm()).thenReturn(scm);
		when(singletons.getProject()).thenReturn(mavenProject);
	}

	@Test
	public void getRemoteUrlScmIsNull() throws PluginException {
		when(mavenProject.getScm()).thenReturn(null);
		rootProject.initialize();
		assertNull(rootProject.getRemoteUrlOrNull());
	}

	@Test
	public void getRemoteUrlNoConnectionsOnScm() throws PluginException {
		rootProject.initialize();
		assertNull(rootProject.getRemoteUrlOrNull());
	}

	@Test
	public void getRemoteUrlUseDeveloperConnection() throws PluginException {
		when(scm.getDeveloperConnection()).thenReturn(DEVELOPER_CONNECTION);
		when(scm.getConnection()).thenReturn(CONNECTION);
		rootProject.initialize();
		assertEquals("ssh://some/developerPath", rootProject.getRemoteUrlOrNull());
	}

	@Test
	public void getRemoteUrlUseConnection() throws PluginException {
		when(scm.getConnection()).thenReturn(CONNECTION);
		rootProject.initialize();
		assertEquals("ssh://some/commonPath", rootProject.getRemoteUrlOrNull());
	}

	@Test
	public void getRemoteUrlIllegalProtocol() {
		when(scm.getDeveloperConnection()).thenReturn("scm:svn:ssh//some/illegal/protocol");
		try {
			rootProject.initialize();
			fail("Exception expected");
		} catch (final PluginException expected) {
			assertEquals("Cannot run the release plugin with a non-Git version control system", expected.getMessage());
			final List<String> messages = expected.getMessages();
			assertEquals(1, messages.size());
			assertEquals("Cannot run the release plugin with a non-Git version control system", expected.getMessage());
			assertEquals("The value in your scm tag is scm:svn:ssh//some/illegal/protocol", messages.get(0));
		}
	}
}
