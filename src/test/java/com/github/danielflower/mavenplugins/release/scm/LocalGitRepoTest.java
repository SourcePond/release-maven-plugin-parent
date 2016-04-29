package com.github.danielflower.mavenplugins.release.scm;

import static com.github.danielflower.mavenplugins.release.scm.GitHelper.scmUrlToRemote;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static scaffolding.TestProject.dirToGitScmReference;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.StoredConfig;
import org.junit.Test;

import scaffolding.TestProject;

public class LocalGitRepoTest {

	TestProject project = TestProject.singleModuleProject();

	@Test
	public void canDetectLocalTags() throws GitAPIException {
		final GitRepository repo = new GitRepository(project.local, null);
		tag(project.local, "some-tag");
		assertThat(repo.hasLocalTag("some-tag"), is(true));
		assertThat(repo.hasLocalTag("some-ta"), is(false));
		assertThat(repo.hasLocalTag("some-tagyo"), is(false));
	}

	@Test
	public void canDetectRemoteTags() throws Exception {
		final GitRepository repo = new GitRepository(project.local, null);
		tag(project.origin, "some-tag");
		assertThat(repo.remoteTagsFrom(tags("blah", "some-tag")), equalTo(asList("some-tag")));
		assertThat(repo.remoteTagsFrom(tags("blah", "some-taggart")), equalTo(emptyList()));
	}

	@Test
	public void usesThePassedInScmUrlToFindRemote() throws Exception {
		final GitRepository repo = new GitRepository(project.local,
				scmUrlToRemote(dirToGitScmReference(project.originDir)));
		tag(project.origin, "some-tag");

		final StoredConfig config = project.local.getRepository().getConfig();
		config.unsetSection("remote", "origin");
		config.save();

		assertThat(repo.remoteTagsFrom(tags("blah", "some-tag")), equalTo(asList("some-tag")));
	}

	@Test
	public void canHaveManyTags() throws GitAPIException {
		final int numberOfTags = 50; // setting this to 1000 works but takes too
										// long
		for (int i = 0; i < numberOfTags; i++) {
			tag(project.local, "this-is-a-tag-" + i);
		}
		project.local.push().setPushTags().call();
		final GitRepository repo = new GitRepository(project.local, null);
		for (int i = 0; i < numberOfTags; i++) {
			final String tagName = "this-is-a-tag-" + i;
			assertThat(repo.hasLocalTag(tagName), is(true));
			assertThat(repo.remoteTagsFrom(tags(tagName)).size(), is(1));
		}
	}

	private static List<AnnotatedTag> tags(final String... tagNames) {
		final List<AnnotatedTag> tags = new ArrayList<AnnotatedTag>();
		for (final String tagName : tagNames) {
			tags.add(AnnotatedTag.create(tagName, "1", 0));
		}
		return tags;
	}

	private static List<String> emptyList() {
		return new ArrayList<String>();
	}

	private static void tag(final Git repo, final String name) throws GitAPIException {
		repo.tag().setAnnotated(true).setName(name).setMessage("Some message").call();
	}
}
