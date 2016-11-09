/*Copyright (C) 2016 Roland Hauser, <sourcepond@gmail.com>
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package ch.sourcepond.maven.release.scm.git;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collection;

import org.apache.maven.plugin.logging.Log;
import org.eclipse.jgit.api.DeleteTagCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.TagCommand;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import ch.sourcepond.maven.release.scm.SCMException;

public class GitProposedTagTest {
	private static final String ANY_NAME = "anyName";
	private static final String ANY_MESSAGE = "anyMessage";
	private static final String ANY_REMOTE_URL = "anyRemoteUrl";
	private static final String ANY_TO_STRING = "anyToString";
	private static final String BUSINESS_VERSION = "1.0";
	private static final long BUILD_NUMBER = 10;
	private final Log log = mock(Log.class);
	private final Git git = mock(Git.class);
	private final Ref ref = mock(Ref.class);
	private final ObjectId id = mock(ObjectId.class);
	private final JSONObject json = mock(JSONObject.class);
	private final TagCommand tagCommand = mock(TagCommand.class);
	private final PushCommand pushCommand = mock(PushCommand.class);
	private final PushResult pushResult = mock(PushResult.class);
	private final Iterable<PushResult> pushResults = asList(pushResult);
	private final RemoteRefUpdate remoteRefUpdate = mock(RemoteRefUpdate.class);
	private final Collection<RemoteRefUpdate> remoteRefUpdates = asList(remoteRefUpdate);
	private final DeleteTagCommand deleteTagCommand = mock(DeleteTagCommand.class);
	private GitProposedTag tag = new GitProposedTag(git, log, ref, ANY_NAME, json, ANY_REMOTE_URL);

	@Before
	public void setup() {
		when(ref.getTarget()).thenReturn(ref);
		when(ref.getObjectId()).thenReturn(id);
		when(json.toJSONString()).thenReturn(ANY_MESSAGE);
		when(json.get(GitProposedTag.BUILD_NUMBER)).thenReturn(String.valueOf(BUILD_NUMBER));
		when(json.get(GitProposedTag.VERSION)).thenReturn(BUSINESS_VERSION);
		when(remoteRefUpdate.toString()).thenReturn(ANY_TO_STRING);
	}

	@Test
	public void verifyGetObjectId() {
		assertEquals(id, tag.getObjectId());
	}

	@Test
	public void verifyGetBusinessVersion() {
		assertEquals(BUSINESS_VERSION, tag.getBusinessVersion());
	}

	@Test
	public void verifyGetReleaseNumber() {
		assertEquals("1.0.10", tag.getReleaseVersion());
	}

	@Test
	public void verifyGetBuildNumber() {
		assertEquals(BUILD_NUMBER, tag.getBuildNumber());
	}

	@Test
	public void verifyName() {
		assertEquals(ANY_NAME, tag.name());
	}

	@Test
	public void saveAtHEADFailed() throws Exception {
		prepareTagAndPush();

		final GitAPIException expected = new CanceledException(ANY_MESSAGE);
		doThrow(expected).when(tagCommand).call();
		try {
			tag.tagAndPush();
			fail("Exception expected");
		} catch (final SCMException e) {
			assertEquals("Ref 'anyName' could be saved at HEAD!", e.getMessage());
			assertSame(expected, e.getCause());
		}
	}

	@Test
	public void tagAndPushFailed() throws Exception {
		prepareTagAndPush();

		final GitAPIException expected = new CanceledException(ANY_MESSAGE);
		doThrow(expected).when(pushCommand).call();
		try {
			tag.tagAndPush();
			fail("Exception expected");
		} catch (final SCMException e) {
			assertEquals("Tag 'anyName' could not be pushed!", e.getMessage());
			assertSame(expected, e.getCause());
		}
	}

	private void prepareSaveAtHEAD() throws Exception {
		when(git.tag()).thenReturn(tagCommand);
		when(tagCommand.setName(ANY_NAME)).thenReturn(tagCommand);
		when(tagCommand.setAnnotated(true)).thenReturn(tagCommand);
		when(tagCommand.setMessage(ANY_MESSAGE)).thenReturn(tagCommand);
		when(tagCommand.call()).thenReturn(ref);
	}

	private void prepareTagAndPush() throws Exception {
		prepareSaveAtHEAD();
		when(git.push()).thenReturn(pushCommand);
		when(pushCommand.add(ref)).thenReturn(pushCommand);
		when(pushCommand.call()).thenReturn(pushResults);
		when(pushResult.getRemoteUpdates()).thenReturn(remoteRefUpdates);
	}

	@Test
	public void tagAndPush_RemoteUrlIsNull() throws Exception {
		tag = new GitProposedTag(git, log, ref, ANY_NAME, json, null);
		prepareTagAndPush();
		tag.tagAndPush();
		verify(log).info(ANY_TO_STRING);
		verify(pushCommand, never()).setRemote(ANY_REMOTE_URL);
	}

	@Test
	public void tagAndPush() throws Exception {
		prepareTagAndPush();
		tag.tagAndPush();
		verify(log).info(ANY_TO_STRING);
		verify(pushCommand).setRemote(ANY_REMOTE_URL);
	}

	@Test
	public void verifyToString() {
		assertEquals("AnnotatedTag{name='anyName', version=1.0, buildNumber=10}", tag.toString());
	}

	@Test
	public void verifyHashCode() {
		assertEquals(ANY_NAME.hashCode(), tag.hashCode());
	}

	@Test
	public void verifyEquals() {
		final GitProposedTag tag1 = new GitProposedTag(git, log, ref, ANY_NAME, json, ANY_REMOTE_URL);
		assertEquals(tag1, tag1);
		assertNotEquals(tag1, null);
		assertNotEquals(tag1, new Object());

		GitProposedTag tag2 = new GitProposedTag(git, log, ref, ANY_NAME, json, ANY_REMOTE_URL);
		assertEquals(tag1, tag2);

		tag2 = new GitProposedTag(git, log, ref, "some different name", json, ANY_REMOTE_URL);
		assertNotEquals(tag1, tag2);
	}

	@Test
	public void getEquivalentVersionOrNull() {
		assertNull(tag.getEquivalentVersionOrNull());
		verifyNoMoreInteractions(git, log, json, ref);
	}

	@Test
	public void hasChanged() {
		assertFalse(tag.hasChanged());
		verifyNoMoreInteractions(git, log, json, ref);
	}

	@Test
	public void makeReleaseable() {
		tag.makeReleaseable();
		verifyNoMoreInteractions(git, log, json, ref);
	}

	private void delete() throws Exception {
		when(deleteTagCommand.setTags(ANY_NAME)).thenReturn(deleteTagCommand);
		when(deleteTagCommand.call()).thenReturn(asList(ANY_NAME));
		when(git.tagDelete()).thenReturn(deleteTagCommand);
		when(git.push()).thenReturn(pushCommand);
		when(pushCommand.add(":refs/tags/" + ANY_NAME)).thenReturn(pushCommand);
		when(pushCommand.call()).thenReturn(pushResults);
		when(pushResult.getRemoteUpdates()).thenReturn(remoteRefUpdates);
	}

	@Test
	public void delete_RemoteUrlIsNull() throws Exception {
		tag = new GitProposedTag(git, log, ref, ANY_NAME, json, null);
		delete();
		tag.delete();
		verify(pushCommand, never()).setRemote(ANY_REMOTE_URL);
		verify(log).info(ANY_TO_STRING);
		verify(log).info("Deleted tag 'anyName' from repository");
	}

	@Test
	public void delete_WithRemoteUrl() throws Exception {
		delete();
		tag.delete();
		verify(pushCommand).setRemote(ANY_REMOTE_URL);
		verify(log).info(ANY_TO_STRING);
		verify(log).info("Deleted tag 'anyName' from repository");
	}

	@Test
	public void delete_NoTagDeleted() throws Exception {
		when(deleteTagCommand.setTags(ANY_NAME)).thenReturn(deleteTagCommand);
		when(deleteTagCommand.call()).thenReturn(asList());
		when(git.tagDelete()).thenReturn(deleteTagCommand);
		tag.delete();
		verifyZeroInteractions(pushCommand);
	}

	@Test
	public void deleteFailed() throws Exception {
		when(deleteTagCommand.setTags(ANY_NAME)).thenReturn(deleteTagCommand);
		when(git.tagDelete()).thenReturn(deleteTagCommand);

		final GitAPIException expected = new CanceledException(ANY_MESSAGE);
		doThrow(expected).when(deleteTagCommand).call();

		try {
			tag.delete();
			fail("Exception expected!");
		} catch (final SCMException e) {
			assertEquals("Remote tag 'anyName' could not be deleted!", e.getMessage());
			assertSame(expected, e.getCause());
		}
	}

}
