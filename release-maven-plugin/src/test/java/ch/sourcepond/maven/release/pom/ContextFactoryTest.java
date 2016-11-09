package ch.sourcepond.maven.release.pom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;

import ch.sourcepond.maven.release.commons.Version;
import ch.sourcepond.maven.release.reactor.Reactor;
import ch.sourcepond.maven.release.reactor.ReleasableModule;


public class ContextFactoryTest {
	private static final String ANY_GROUP_ID = "anyGroupId";
	private static final String ANY_ARTIFACT_ID = "anyArtifactId";
	private static final String TEST_STRING = "test";
	private final Reactor reactor = mock(Reactor.class);
	private final Model model = mock(Model.class);
	private final MavenProject project = mock(MavenProject.class);
	private Context context;

	@Before
	public void setup() {
		context = new ContextFactory().newContext(reactor, project, model, false);
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
	public void getModel() {
		assertSame(model, context.getModel());
	}

	@Test
	public void needsOwnVersion() {
		assertFalse(context.needsOwnVersion());
		context.setNeedsOwnVersion(true);
		assertTrue(context.needsOwnVersion());
	}

	@Test
	public void dependencyUpdated() {
		assertFalse(context.dependencyUpdated());
		context.setDependencyUpdated();
		assertTrue(context.dependencyUpdated());
		context.setDependencyUpdated();
		assertTrue(context.dependencyUpdated());
	}

	@Test
	public void getVersionToDependOn_IncrementSnapshot_HasChanged() throws Exception {
		context = new ContextFactory().newContext(reactor, project, model, true);
		final ReleasableModule module = mock(ReleasableModule.class);
		final Version version = mock(Version.class);
		when(version.hasChanged()).thenReturn(true);
		when(module.getVersion()).thenReturn(version);
		when(version.getNextDevelopmentVersion()).thenReturn(TEST_STRING);
		when(reactor.find(ANY_GROUP_ID, ANY_ARTIFACT_ID)).thenReturn(module);
		assertEquals(TEST_STRING, context.getVersionToDependOn(ANY_GROUP_ID, ANY_ARTIFACT_ID));
	}

	@Test
	public void getVersionToDependOn_IncrementSnapshot_HasNotChanged() throws Exception {
		context = new ContextFactory().newContext(reactor, project, model, true);
		final ReleasableModule module = mock(ReleasableModule.class);
		final Version version = mock(Version.class);
		when(version.hasChanged()).thenReturn(false);
		when(module.getVersion()).thenReturn(version);
		when(version.getDevelopmentVersion()).thenReturn(TEST_STRING);
		when(reactor.find(ANY_GROUP_ID, ANY_ARTIFACT_ID)).thenReturn(module);
		assertEquals(TEST_STRING, context.getVersionToDependOn(ANY_GROUP_ID, ANY_ARTIFACT_ID));
	}

	@Test
	public void getVersionToDependOn_ReleaseVersion() throws Exception {
		final ReleasableModule module = mock(ReleasableModule.class);
		final Version version = mock(Version.class);
		when(module.getVersion()).thenReturn(version);
		when(version.getReleaseVersion()).thenReturn(TEST_STRING);
		when(reactor.find(ANY_GROUP_ID, ANY_ARTIFACT_ID)).thenReturn(module);
		assertEquals(TEST_STRING, context.getVersionToDependOn(ANY_GROUP_ID, ANY_ARTIFACT_ID));
	}

	@Test
	public void getVersionToDependOn_EquivalentVersion() throws Exception {
		final ReleasableModule module = mock(ReleasableModule.class);
		final Version version = mock(Version.class);
		when(module.getVersion()).thenReturn(version);
		when(version.getEquivalentVersionOrNull()).thenReturn(TEST_STRING);
		when(reactor.find(ANY_GROUP_ID, ANY_ARTIFACT_ID)).thenReturn(module);
		assertEquals(TEST_STRING, context.getVersionToDependOn(ANY_GROUP_ID, ANY_ARTIFACT_ID));
	}

	@Test
	public void hasNotChanged_ParentIsNull() {
		assertTrue(context.hasNotChanged(null));
	}

	@Test
	public void hasNotChanged_NoReleasableModule() {
		assertTrue(context.hasNotChanged(mock(Parent.class)));
	}

	@Test
	public void hasNotChanged_VersionNotChanged() {
		final ReleasableModule module = mock(ReleasableModule.class);
		when(module.getGroupId()).thenReturn(ANY_GROUP_ID);
		when(module.getArtifactId()).thenReturn(ANY_ARTIFACT_ID);
		final Version version = mock(Version.class);
		when(module.getVersion()).thenReturn(version);
		when(reactor.findByLabel(ANY_GROUP_ID + ":" + ANY_ARTIFACT_ID)).thenReturn(module);
		final Parent parent = mock(Parent.class);
		when(parent.getGroupId()).thenReturn(ANY_GROUP_ID);
		when(parent.getArtifactId()).thenReturn(ANY_ARTIFACT_ID);
		assertTrue(context.hasNotChanged(parent));
	}

	@Test
	public void hasNotChanged_VersionChanged() {
		final ReleasableModule module = mock(ReleasableModule.class);
		when(module.getGroupId()).thenReturn(ANY_GROUP_ID);
		when(module.getArtifactId()).thenReturn(ANY_ARTIFACT_ID);
		final Version version = mock(Version.class);
		when(version.hasChanged()).thenReturn(true);
		when(module.getVersion()).thenReturn(version);
		when(reactor.findByLabel(ANY_GROUP_ID + ":" + ANY_ARTIFACT_ID)).thenReturn(module);
		final Parent parent = mock(Parent.class);
		when(parent.getGroupId()).thenReturn(ANY_GROUP_ID);
		when(parent.getArtifactId()).thenReturn(ANY_ARTIFACT_ID);
		assertFalse(context.hasNotChanged(parent));
	}
}