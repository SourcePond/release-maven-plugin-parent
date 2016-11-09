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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ch.sourcepond.maven.release.scm.ProposedTag;
import ch.sourcepond.maven.release.scm.SCMException;
import ch.sourcepond.maven.release.scm.git.GitProposedTags;
import ch.sourcepond.maven.release.version.Version;

public class DefaultProposedTagsTest {
	private static final String ANY_TAG = "anyTag";
	private static final String ANY_VERSION = "1.0.0";
	private static final String ANY_REMOTE_URL = "anyRemoteUrl";
	private final Log log = mock(Log.class);
	private final ProposedTag tag = mock(ProposedTag.class);
	private final Map<String, ProposedTag> proposedTags = new HashMap<>();
	private final GitProposedTags tags = new GitProposedTags(log, ANY_REMOTE_URL, proposedTags);

	@Before
	public void setup() {
		proposedTags.put(ANY_TAG + "/" + ANY_VERSION, tag);
	}

	@Test
	public void tagAndPushRepo() throws Exception {
		tags.tagAndPushRepo();
		verify(tag).tagAndPush(ANY_REMOTE_URL);
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
		tags.revertTagsAndPush();
		verify(tag).delete(ANY_REMOTE_URL);
	}
	
	@Test
	public void revertTagsAndPush_LogWarnIfFailed() throws Exception {
		final SCMException expected  = new SCMException("expected");
		doThrow(expected).when(tag).delete(ANY_REMOTE_URL);
		tags.revertTagsAndPush();
		verify(log).warn(expected);
	}
}
