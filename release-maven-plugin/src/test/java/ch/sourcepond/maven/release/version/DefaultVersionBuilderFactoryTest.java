package ch.sourcepond.maven.release.version;

import static ch.sourcepond.maven.release.version.DefaultVersionBuilder.SNAPSHOT_EXTENSION;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;

import ch.sourcepond.maven.release.commons.Version;
import ch.sourcepond.maven.release.config.Configuration;

public class DefaultVersionBuilderFactoryTest {
	private static final String BUSINESS_VERSION = "1.0";
	private static final String DEVELOPMENT_VERSION = BUSINESS_VERSION + SNAPSHOT_EXTENSION;
	private static final Long BUILD_NUMBER = 9L;
	private static final String CHANGED_DEPENDENCY = "changedDependency";
	private static final String RELATIVE_PATH_TO_MODULE = "relativePathToModule";
	private static final String EQUIVALENT_VERSION = "equivalentVersion";
	private final BuildNumberFinder finder = mock(BuildNumberFinder.class);
	private final ChangeDetectorFactory detectorFactory = mock(ChangeDetectorFactory.class);
	private final ChangeDetector detector = mock(ChangeDetector.class);
	private final MavenProject project = mock(MavenProject.class);
	private final Configuration configuratinon = mock(Configuration.class);
	private final VersionBuilder builder = new DefaultVersionBuilder(finder, configuratinon, detectorFactory);

	@Before
	public void setup() throws VersionException {
		when(detectorFactory.newDetector()).thenReturn(detector);
		when(detector.setProject(project)).thenReturn(detector);
		when(detector.setActualBuildNumber(BUILD_NUMBER)).thenReturn(detector);
		when(detector.setChangedDependency(CHANGED_DEPENDENCY)).thenReturn(detector);
		when(detector.setRelativePathToModule(RELATIVE_PATH_TO_MODULE)).thenReturn(detector);
		when(detector.setBusinessVersion(BUSINESS_VERSION)).thenReturn(detector);
		when(detector.equivalentVersionOrNull()).thenReturn(EQUIVALENT_VERSION);

		when(project.getVersion()).thenReturn(DEVELOPMENT_VERSION);
		builder.setProject(project);
		builder.setChangedDependency(CHANGED_DEPENDENCY);
		builder.setRelativePath(RELATIVE_PATH_TO_MODULE);
	}

	@Test
	public void buildWhenBuildNumberIsSet() throws VersionException {
		when(detector.setActualBuildNumber(11)).thenReturn(detector);
		when(configuratinon.getBuildNumberOrNull()).thenReturn(11l);
		final Version version = builder.build();
		assertEquals(11l, version.getBuildNumber());
	}

	@Test
	public void buildWhenUseLastDigitAsBuildNumberAndBuildNumberAreSet_LastDigitIsHigher() throws VersionException {
		when(project.getVersion()).thenReturn("1.0.11-SNAPSHOT");
		when(detector.setActualBuildNumber(11)).thenReturn(detector);
		when(configuratinon.isIncrementSnapshotVersionAfterRelease()).thenReturn(true);
		when(configuratinon.getBuildNumberOrNull()).thenReturn(9l);
		final Version version = builder.build();
		assertEquals(11l, version.getBuildNumber());
	}

	@Test
	public void buildWhenUseLastDigitAsBuildNumberAndBuildNumberAreSet_BuildNumberIsHigher() throws VersionException {
		when(project.getVersion()).thenReturn("1.0.9-SNAPSHOT");
		when(detector.setActualBuildNumber(11l)).thenReturn(detector);
		when(configuratinon.isIncrementSnapshotVersionAfterRelease()).thenReturn(true);
		when(configuratinon.getBuildNumberOrNull()).thenReturn(11l);
		final Version version = builder.build();
		assertEquals(11l, version.getBuildNumber());
	}

	@Test
	public void buildWhenUseLastDigitAsBuildNumberAndBuildNumberNotSet() throws VersionException {
		when(configuratinon.getBuildNumberOrNull()).thenReturn(null);
		when(project.getVersion()).thenReturn("1.0.11-SNAPSHOT");
		when(detector.setActualBuildNumber(11l)).thenReturn(detector);
		when(configuratinon.isIncrementSnapshotVersionAfterRelease()).thenReturn(true);
		final Version version = builder.build();
		assertEquals(11l, version.getBuildNumber());
	}

	@Test
	public void verifyEvaluateBuildNumber() throws VersionException {
		when(finder.findBuildNumber(project, BUSINESS_VERSION)).thenReturn(10l);
		when(detector.setActualBuildNumber(10l)).thenReturn(detector);
		when(configuratinon.getBuildNumberOrNull()).thenReturn(null);
		final Version version = builder.build();
		assertEquals(10l, version.getBuildNumber());
	}
}