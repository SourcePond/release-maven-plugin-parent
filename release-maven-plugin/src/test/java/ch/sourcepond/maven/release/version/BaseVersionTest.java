package ch.sourcepond.maven.release.version;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import ch.sourcepond.maven.release.commons.BaseVersion;

/**
 *
 */
public class BaseVersionTest {
	private final BaseVersion version = mock(BaseVersion.class);

	@Test
	public void verifyNextDevelopmentVersion() {
		when(version.getBusinessVersion()).thenReturn("1.0");
		when(version.getBuildNumber()).thenReturn(3l);
		assertEquals("1.0.4-SNAPSHOT", version.getNextDevelopmentVersion());
	}

	@Test
	public void verifyDevelopmentVersion() {
		when(version.getBusinessVersion()).thenReturn("1.0");
		when(version.getBuildNumber()).thenReturn(3l);
		assertEquals("1.0.3-SNAPSHOT", version.getDevelopmentVersion());
	}
}