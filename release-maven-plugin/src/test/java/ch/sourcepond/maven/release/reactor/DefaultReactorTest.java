package ch.sourcepond.maven.release.reactor;

import static ch.sourcepond.integrationtest.utils.ReleasableModuleBuilder.aModule;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Iterator;

import org.apache.maven.plugin.logging.Log;
import org.junit.Assert;
import org.junit.Test;

import ch.sourcepond.maven.release.commons.Version;



public class DefaultReactorTest {
	private final Log log = mock(Log.class);
	private final DefaultReactor reactor = new DefaultReactor(log);

	@Test
	public void canFindModulesByGroupAndArtifactName() throws Exception {
		final ReleasableModule arty = aModule().withGroupId("my.great.group").withArtifactId("some-arty").build();
		reactor.addReleasableModule(aModule().build());
		reactor.addReleasableModule(arty);
		reactor.addReleasableModule(aModule().build());
		assertThat(reactor.find("my.great.group", "some-arty"), is(arty));
		assertThat(reactor.findByLabel("my.great.group:some-arty"), is(arty));
	}

	@Test
	public void findOrReturnNullReturnsNullIfNotFound() throws Exception {
		reactor.addReleasableModule(aModule().build());
		reactor.addReleasableModule(aModule().build());
		assertThat(reactor.findByLabel("my.great.group:some-arty"), is(nullValue()));
	}

	@Test
	public void ifNotFoundThenAUnresolvedSnapshotDependencyExceptionIsThrown() throws Exception {
		reactor.addReleasableModule(aModule().build());
		reactor.addReleasableModule(aModule().build());
		try {
			reactor.find("my.great.group", "some-arty");
			Assert.fail("Should have thrown");
		} catch (final UnresolvedSnapshotDependencyException e) {
			assertThat(e.getMessage(), equalTo("Could not find my.great.group:some-arty"));
		}
	}

	@Test
	public void finalizeReleaseVersionsWhenReleasableModuleIsAvailable() {
		final ReleasableModule willBeReleased = mock(ReleasableModule.class);
		final Version version = mock(Version.class);
		when(version.hasChanged()).thenReturn(true);
		when(willBeReleased.getVersion()).thenReturn(version);
		reactor.addReleasableModule(willBeReleased);
		final Iterator<ReleasableModule> modules = reactor.finalizeReleaseVersions().iterator();
		assertSame(willBeReleased, modules.next());
		assertFalse(modules.hasNext());
	}

	@Test
	public void finalizeReleaseVersionsWhenNoReleasableModuleHasBeenAdded() {
		final ReleasableModule willNotBeReleased = mock(ReleasableModule.class);
		final Version version = mock(Version.class);
		when(willNotBeReleased.getVersion()).thenReturn(version);
		reactor.addReleasableModule(willNotBeReleased);
		final Iterator<ReleasableModule> modules = reactor.finalizeReleaseVersions().iterator();
		verify(version).makeReleaseable();
		assertSame(willNotBeReleased, modules.next());
		assertFalse(modules.hasNext());
	}
}
