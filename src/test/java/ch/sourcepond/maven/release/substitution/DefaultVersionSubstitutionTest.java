package ch.sourcepond.maven.release.substitution;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.junit.Test;

public class DefaultVersionSubstitutionTest {
	private static final String ANY_GROUP_ID = "anyGroupId";
	private static final String ANY_ARTIFACT_ID = "anyArtifactId";
	private static final String ANY_VERSION_PROPERTY = "${anyVersionProperty}";
	private static final String EXPECTED_GROUP_ID = "expectedGroupId";
	private static final String EXPECTED_ARTIFACT_ID = "expectedArtifactId";
	private static final String EXPECTED_VERSION = "10.0";
	private final MavenProject project = mock(MavenProject.class);
	private final DefaultVersionSubstitution substitution = new DefaultVersionSubstitution(new DependencyAdapter(),
			new PluginAdapter());

	@Test
	public void getActualDependencyVersion() {
		final Dependency originalDependency = mock(Dependency.class);
		when(originalDependency.getGroupId()).thenReturn(EXPECTED_GROUP_ID);
		when(originalDependency.getArtifactId()).thenReturn(EXPECTED_ARTIFACT_ID);
		when(originalDependency.getVersion()).thenReturn(ANY_VERSION_PROPERTY);

		final Dependency substitutedDependency = mock(Dependency.class);
		when(substitutedDependency.getGroupId()).thenReturn(EXPECTED_GROUP_ID);
		when(substitutedDependency.getArtifactId()).thenReturn(EXPECTED_ARTIFACT_ID);
		when(substitutedDependency.getVersion()).thenReturn(EXPECTED_VERSION);

		when(project.getDependencies()).thenReturn(asList(substitutedDependency));
		assertEquals(EXPECTED_VERSION, substitution.getActualVersionOrNull(project, originalDependency));
	}

	@Test
	public void getActualDependencyVersionNoMatchingSubstitution() {
		final Dependency originalDependency = mock(Dependency.class);
		when(originalDependency.getGroupId()).thenReturn(ANY_GROUP_ID);
		when(originalDependency.getArtifactId()).thenReturn(ANY_ARTIFACT_ID);
		when(originalDependency.getVersion()).thenReturn(ANY_VERSION_PROPERTY);

		final Dependency substitutedDependency = mock(Dependency.class);
		when(substitutedDependency.getGroupId()).thenReturn(EXPECTED_GROUP_ID);
		when(substitutedDependency.getArtifactId()).thenReturn(EXPECTED_ARTIFACT_ID);
		when(substitutedDependency.getVersion()).thenReturn(EXPECTED_VERSION);

		when(project.getDependencies()).thenReturn(asList(substitutedDependency));

		assertNull(substitution.getActualVersionOrNull(project, originalDependency));
	}

	@Test
	public void getActualPluginVersion() {
		final Plugin originalPlugin = mock(Plugin.class);
		when(originalPlugin.getGroupId()).thenReturn(EXPECTED_GROUP_ID);
		when(originalPlugin.getArtifactId()).thenReturn(EXPECTED_ARTIFACT_ID);
		when(originalPlugin.getVersion()).thenReturn(ANY_VERSION_PROPERTY);

		final Plugin substitutedPlugins = mock(Plugin.class);
		when(substitutedPlugins.getGroupId()).thenReturn(EXPECTED_GROUP_ID);
		when(substitutedPlugins.getArtifactId()).thenReturn(EXPECTED_ARTIFACT_ID);
		when(substitutedPlugins.getVersion()).thenReturn(EXPECTED_VERSION);

		when(project.getBuildPlugins()).thenReturn(asList(substitutedPlugins));
		assertEquals(EXPECTED_VERSION, substitution.getActualVersionOrNull(project, originalPlugin));
	}

	@Test
	public void getActualPluginVersionNoMatchingSubstitution() {
		final Plugin originalPlugin = mock(Plugin.class);
		when(originalPlugin.getGroupId()).thenReturn(ANY_GROUP_ID);
		when(originalPlugin.getArtifactId()).thenReturn(ANY_ARTIFACT_ID);
		when(originalPlugin.getVersion()).thenReturn(ANY_VERSION_PROPERTY);

		final Plugin substitutedPlugins = mock(Plugin.class);
		when(substitutedPlugins.getGroupId()).thenReturn(EXPECTED_GROUP_ID);
		when(substitutedPlugins.getArtifactId()).thenReturn(EXPECTED_ARTIFACT_ID);
		when(substitutedPlugins.getVersion()).thenReturn(EXPECTED_VERSION);

		when(project.getBuildPlugins()).thenReturn(asList(substitutedPlugins));

		assertNull(substitution.getActualVersionOrNull(project, originalPlugin));
	}
}