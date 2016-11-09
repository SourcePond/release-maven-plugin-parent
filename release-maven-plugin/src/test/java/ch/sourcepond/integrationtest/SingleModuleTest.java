package ch.sourcepond.integrationtest;

import static ch.sourcepond.integrationtest.utils.ExactCountMatcher.oneOf;
import static ch.sourcepond.integrationtest.utils.GitMatchers.hasCleanWorkingDirectory;
import static ch.sourcepond.integrationtest.utils.GitMatchers.hasTag;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Ignore;
import org.junit.Test;

import ch.sourcepond.integrationtest.utils.TestProject;
import ch.sourcepond.maven.release.commons.Version;
import ch.sourcepond.maven.release.scm.ProposedTags;
import ch.sourcepond.maven.release.scm.ProposedTagsBuilder;
import ch.sourcepond.maven.release.scm.SCMRepository;
import ch.sourcepond.maven.release.scm.git.GitConfig;
import ch.sourcepond.maven.release.scm.git.GitFactory;
import ch.sourcepond.maven.release.scm.git.GitRepository;


public class SingleModuleTest extends E2ETest {

	final String buildNumber = String.valueOf(System.currentTimeMillis());
	final String expected = "1.0." + buildNumber;
	final TestProject testProject = TestProject.singleModuleProject();

	@Test
	public void canUpdateSnapshotVersionToReleaseVersionAndInstallToLocalRepo() throws Exception {
		final List<String> outputLines = testProject.mvnRelease(buildNumber);
		assertThat(outputLines, oneOf(containsString("Going to release single-module " + expected)));
		assertThat(outputLines, oneOf(containsString("Hello from version " + expected + "!")));

		assertArtifactInLocalRepo("ch.sourcepond.maven.plugins.testprojects", "single-module", expected);

		assertThat(new File(testProject.localDir, "target/single-module-" + expected + "-package.jar").exists(),
				is(true));
	}

	// TODO: Do not work with plugin classes as this causes
	// NoClassDefFoundErrors
	@Test
	@Ignore
	public void theBuildNumberIsOptionalAndWillStartAt0AndThenIncrementTakingIntoAccountLocalAndRemoteTags()
			throws Exception {
		testProject.mvn("releaser:release");
		assertThat(testProject.local, hasTag("single-module-1.0.0"));
		testProject.mvn("releaser:release");
		assertThat(testProject.local, hasTag("single-module-1.0.1"));

		final GitFactory gitFactory = mock(GitFactory.class);
		when(gitFactory.newGit()).thenReturn(testProject.local);
		final SCMRepository repo = new GitRepository(mock(Log.class), mock(GitConfig.class));
		final ProposedTagsBuilder builder = repo.newProposedTagsBuilder();
		final Version version = mock(Version.class);
		when(version.getBusinessVersion()).thenReturn("1.0");
		when(version.getBuildNumber()).thenReturn(2l);
		builder.add("single-module-1.0.2", version).build().getTag("single-module-1.0.2", version).saveAtHEAD();
		testProject.mvn("releaser:release");
		assertThat(testProject.local, hasTag("single-module-1.0.3"));

		final Version version1 = mock(Version.class);
		when(version1.getBusinessVersion()).thenReturn("1.0");
		when(version1.getBuildNumber()).thenReturn(4l);
		builder.add("single-module-1.0.4", version1);
		final Version version2 = mock(Version.class);
		when(version2.getBusinessVersion()).thenReturn("1.0");
		when(version2.getBuildNumber()).thenReturn(5l);
		builder.add("unrelated-module-1.0.5", version2);
		final ProposedTags tags = builder.build();
		tags.getTag("single-module-1.0.4", version1).saveAtHEAD();
		tags.getTag("unrelated-module-1.0.5", version2).saveAtHEAD();

		testProject.mvn("releaser:release");
		assertThat(testProject.local, hasTag("single-module-1.0.5"));

	}

	@Test
	public void theLocalAndRemoteGitReposAreTaggedWithTheModuleNameAndVersion()
			throws IOException, InterruptedException {
		testProject.mvnRelease(buildNumber);
		final String expectedTag = "single-module-" + expected;
		assertThat(testProject.local, hasTag(expectedTag));
		assertThat(testProject.origin, hasTag(expectedTag));
	}

	@Test
	public void thePomChangesAreRevertedAfterTheRelease() throws IOException, InterruptedException {
		final ObjectId originHeadAtStart = head(testProject.origin);
		final ObjectId localHeadAtStart = head(testProject.local);
		assertThat(originHeadAtStart, equalTo(localHeadAtStart));
		testProject.mvnRelease(buildNumber);
		assertThat(head(testProject.origin), equalTo(originHeadAtStart));
		assertThat(head(testProject.local), equalTo(localHeadAtStart));
		assertThat(testProject.local, hasCleanWorkingDirectory());
	}

	private ObjectId head(final Git git) throws IOException {
		return git.getRepository().getRef("HEAD").getObjectId();
	}

}
