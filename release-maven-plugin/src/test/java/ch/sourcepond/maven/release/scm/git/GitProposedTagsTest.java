package ch.sourcepond.maven.release.scm.git;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;

import ch.sourcepond.maven.release.commons.Version;
import ch.sourcepond.maven.release.scm.ProposedTag;
import ch.sourcepond.maven.release.scm.SCMException;

public class GitProposedTagsTest {
	private static final String ANY_TAG = "anyTag";
	private static final String ANY_VERSION = "1.0.0";
	private final Log log = mock(Log.class);
	private final ProposedTag tag = mock(ProposedTag.class);
	private final Map<String, ProposedTag> proposedTags = new HashMap<>();
	private final GitProposedTags tags = new GitProposedTags(log, proposedTags);

	@Before
	public void setup() {
		proposedTags.put(ANY_TAG + "/" + ANY_VERSION, tag);
	}

	@Test
	public void tagAndPushRepo() throws Exception {
		tags.tag();
		verify(tag).tagAndPush();
	}

	@Test
	public void getTag() throws Exception {
		final Version version = mock(Version.class);
		when(version.getReleaseVersion()).thenReturn(ANY_VERSION);
		assertSame(tag, tags.getTag(ANY_TAG, version));
	}

	@Test
	public void getTag_NotFound() throws Exception {
		final Version version = mock(Version.class);
		when(version.getReleaseVersion()).thenReturn(ANY_VERSION);

		try {
			tags.getTag("notAvailable", version);
			fail("Exception expected!");
		} catch (final SCMException expected) {
			assertEquals("No proposed tag registered for notAvailable/1.0.0", expected.getMessage());
		}
	}

	@Test
	public void iterator() {
		final Iterator<ProposedTag> it = tags.iterator();
		assertSame(tag, it.next());
		assertFalse(it.hasNext());
	}

	@Test
	public void revertTagsAndPush() throws Exception {
		tags.undoTag();
		verify(tag).delete();
	}

	@Test
	public void revertTagsAndPush_LogWarnIfFailed() throws Exception {
		final SCMException expected = new SCMException("expected");
		doThrow(expected).when(tag).delete();
		tags.undoTag();
		verify(log).warn(expected);
	}
}
