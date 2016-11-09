package ch.sourcepond.maven.release.scm.git;

import static ch.sourcepond.maven.release.scm.git.AnnotatedTagFinderTest.saveFileInModule;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.maven.plugin.logging.Log;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListTagCommand;
import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.lib.Ref;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ch.sourcepond.integrationtest.utils.TestProject;
import ch.sourcepond.maven.release.scm.ProposedTag;
import ch.sourcepond.maven.release.scm.SCMException;

@Ignore
public class GitRepositoryTest {
	private static final String REFS_TAGS = "refs/tags/";
	private static final String ANY_REMOTE_URL = "anyRemoteUrl";
	private static final String TAG_TO_CHECK = "tagToCheck";
	private final Log log = mock(Log.class);
	private final GitFactory gitFactory = mock(GitFactory.class);
	private final GitConfig config = mock(GitConfig.class);
	private final ListTagCommand cmd = mock(ListTagCommand.class);
	private final Git git = mock(Git.class);
	private final Ref ref = mock(Ref.class);
	private final GitRepository repository = new GitRepository(log, config);

	@Before
	public void setup() throws Exception {
		when(git.tagList()).thenReturn(cmd);
		when(cmd.call()).thenReturn(asList(ref));
		when(ref.getName()).thenReturn(format("%s%s", REFS_TAGS, TAG_TO_CHECK));
		when(gitFactory.newGit()).thenReturn(git);
	}

	@Test
	public void hasLocalTag() throws Exception {
		assertTrue(repository.hasLocalTag(TAG_TO_CHECK));
		assertFalse(repository.hasLocalTag("someOtherTag"));
	}

	@Test
	public void checkValidRefName() throws SCMException {
		// This should be ok
		repository.checkValidRefName("1.0.0");

		try {
			repository.checkValidRefName("\\");
			fail("Exception expected");
		} catch (final SCMException expected) {
			expected.getMessage().equals(format(GitRepository.INVALID_REF_NAME_MESSAGE, "\\"));
		}
	}

	@Test
	public void canDetectRemoteTags() throws Exception {
		final LsRemoteCommand cmd = mock(LsRemoteCommand.class);
		when(git.lsRemote()).thenReturn(cmd);
		when(cmd.setTags(true)).thenReturn(cmd);
		when(cmd.setHeads(false)).thenReturn(cmd);
		when(cmd.call()).thenReturn(Arrays.asList(ref));
		final Collection<Ref> refs = repository.allRemoteTags();
		assertEquals(1, refs.size());
		assertEquals(ref, refs.iterator().next());
		verify(cmd).setRemote(ANY_REMOTE_URL);
	}

	@Test
	public void canDetectIfFilesHaveBeenChangedForAModuleSinceSomeSpecificTag() throws Exception {
		final TestProject project = TestProject.independentVersionsProject();

		final ProposedTag tag1 = saveFileInModule(project, "console-app", "1.2", 3);
		final ProposedTag tag2 = saveFileInModule(project, "core-utils", "2", 0);
		final ProposedTag tag3 = saveFileInModule(project, "console-app", "1.2", 4);

		final GitRepository detector = new GitRepository(log, config);
		when(gitFactory.newGit()).thenReturn(project.local);

		assertThat(detector.hasChangedSince("core-utils", noChildModules(), asList(tag2)), is(false));
		assertThat(detector.hasChangedSince("console-app", noChildModules(), asList(tag2)), is(true));
		assertThat(detector.hasChangedSince("console-app", noChildModules(), asList(tag3)), is(false));
	}

	@Test
	public void canDetectThingsInTheRoot() throws Exception {
		final TestProject simple = TestProject.singleModuleProject();
		final ProposedTag tag1 = saveFileInModule(simple, ".", "1.0", 1);
		simple.commitRandomFile(".");
		final GitRepository detector = new GitRepository(log, config);
		when(gitFactory.newGit()).thenReturn(simple.local);
		assertThat(detector.hasChangedSince(".", noChildModules(), asList(tag1)), is(true));

		final ProposedTag tag2 = saveFileInModule(simple, ".", "1.0", 2);
		assertThat(detector.hasChangedSince(".", noChildModules(), asList(tag2)), is(false));
	}

	@Test
	public void canDetectChangesAfterTheLastTag() throws Exception {
		final TestProject project = TestProject.independentVersionsProject();

		saveFileInModule(project, "console-app", "1.2", 3);
		saveFileInModule(project, "core-utils", "2", 0);
		final ProposedTag tag3 = saveFileInModule(project, "console-app", "1.2", 4);
		project.commitRandomFile("console-app");

		final GitRepository detector = new GitRepository(log, config);
		when(gitFactory.newGit()).thenReturn(project.local);
		assertThat(detector.hasChangedSince("console-app", noChildModules(), asList(tag3)), is(true));
	}

	@Test
	public void canIgnoreModuleFolders() throws Exception {
		final TestProject project = TestProject.independentVersionsProject();

		saveFileInModule(project, "console-app", "1.2", 3);
		saveFileInModule(project, "core-utils", "2", 0);
		final ProposedTag tag3 = saveFileInModule(project, "console-app", "1.2", 4);
		project.commitRandomFile("console-app");

		final GitRepository detector = new GitRepository(log, config);
		when(gitFactory.newGit()).thenReturn(project.local);
		assertThat(detector.hasChangedSince("console-app", asList("console-app"), asList(tag3)), is(false));
	}

	private static java.util.List<String> noChildModules() {
		return new ArrayList<>();
	}
}
