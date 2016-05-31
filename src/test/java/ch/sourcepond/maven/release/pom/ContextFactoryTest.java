package ch.sourcepond.maven.release.pom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;

import ch.sourcepond.maven.release.reactor.Reactor;
import ch.sourcepond.maven.release.reactor.ReleasableModule;
import ch.sourcepond.maven.release.version.Version;

public class ContextFactoryTest {
	private static final String ANY_GROUP_ID = "anyGroupId";
	private static final String ANY_ARTIFACT_ID = "anyArtifactId";
	private static final String TEST_STRING = "test";
	private final Reactor reactor = mock(Reactor.class);
	private final MavenProject project = mock(MavenProject.class);
	private Context context;

	@Before
	public void setup() {
		context = new ContextFactory().newContext(reactor, project, false);
	}

	@Test
	public void verfiyAddGetError() {
		context.addError("%s 1", TEST_STRING);
		context.addError("%s 2", TEST_STRING);
		final List<String> errors = context.getErrors();
		assertEquals(2, errors.size());
		assertEquals("test 1", errors.get(0));
		assertEquals("test 2", errors.get(1));
	}

	@Test
	public void getProject() {
		assertSame(project, context.getProject());
	}

	@Test
	public void getVersionToDependOn() throws Exception {
		final ReleasableModule module = mock(ReleasableModule.class);
		final Version version = mock(Version.class);
		when(module.getVersion()).thenReturn(version);
		when(version.getReleaseVersion()).thenReturn(TEST_STRING);
		when(reactor.find(ANY_GROUP_ID, ANY_ARTIFACT_ID)).thenReturn(module);
		assertEquals(TEST_STRING, context.getVersionToDependOn(ANY_GROUP_ID, ANY_ARTIFACT_ID));
	}

}