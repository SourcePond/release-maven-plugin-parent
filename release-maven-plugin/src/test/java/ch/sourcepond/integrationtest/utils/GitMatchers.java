package ch.sourcepond.integrationtest.utils;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class GitMatchers {

	static final String REFS_TAGS = "refs/tags/";

	public static Matcher<Git> hasTag(final String tag) {
		return new TypeSafeDiagnosingMatcher<Git>() {
			@Override
			protected boolean matchesSafely(final Git repo, final Description mismatchDescription) {
				try {
					mismatchDescription.appendValueList("a git repo with tags: ", ", ", "",
							repo.getRepository().getTags().keySet());
					for (final Ref ref : repo.tagList().call()) {
						final String currentTag = ref.getName().replace(REFS_TAGS, "");
						if (tag.equals(currentTag)) {
							return true;
						}
					}
					return false;
				} catch (final GitAPIException e) {
					throw new RuntimeException("Couldn't access repo", e);
				}
			}

			@Override
			public void describeTo(final Description description) {
				description.appendText("a git repo with the tag " + tag);
			}
		};
	}

	public static Matcher<Git> hasCleanWorkingDirectory() {
		return new TypeSafeDiagnosingMatcher<Git>() {
			@Override
			protected boolean matchesSafely(final Git git, final Description mismatchDescription) {
				try {
					final Status status = git.status().call();
					if (!status.isClean()) {
						final String start = "Uncommitted changes in ";
						final String end = " at " + git.getRepository().getWorkTree().getAbsolutePath();
						mismatchDescription.appendValueList(start, ", ", end, status.getUncommittedChanges());
					}
					return status.isClean();
				} catch (final GitAPIException e) {
					throw new RuntimeException("Error checking git status", e);
				}
			}

			@Override
			public void describeTo(final Description description) {
				description.appendText("A git directory with no staged or unstaged changes");
			}
		};
	}
}
