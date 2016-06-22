package ch.sourcepond.integrationtest;

import static ch.sourcepond.integrationtest.utils.ExactCountMatcher.oneOf;
import static ch.sourcepond.integrationtest.utils.ExactCountMatcher.threeOf;
import static ch.sourcepond.integrationtest.utils.ExactCountMatcher.twoOf;
import static ch.sourcepond.integrationtest.utils.GitMatchers.hasCleanWorkingDirectory;
import static ch.sourcepond.integrationtest.utils.GitMatchers.hasTag;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import ch.sourcepond.integrationtest.utils.MavenExecutionException;
import ch.sourcepond.integrationtest.utils.MvnRunner;
import ch.sourcepond.integrationtest.utils.TestProject;

public class IncrementSnapshotVersionAfterReleaseTest extends E2ETest {

	final String buildNumber = String.valueOf(System.currentTimeMillis());
	final String expectedParentVersion = "1.0." + buildNumber;
	final String expectedCoreVersion = "2.0." + buildNumber;
	final String expectedAppVersion = "3.2." + buildNumber;
	final TestProject testProject = TestProject.incrementSnapshotVersionAfterRelease();

	@Test
	public void buildsAndInstallsAndTagsAllModules() throws Exception {
		buildsEachProjectOnceAndOnlyOnce(testProject.mvnRelease(buildNumber));
		installsAllModulesIntoTheRepoWithTheBuildNumber();
		theLocalAndRemoteGitReposAreTaggedWithTheModuleNameAndVersion();
	}

	private void buildsEachProjectOnceAndOnlyOnce(final List<String> commandOutput) throws Exception {
		assertThat(commandOutput,
				allOf(oneOf(containsString(
						"Going to release increment-snapshot-version-after-release " + expectedParentVersion)),
				twoOf(containsString("Building increment-snapshot-version-after-release")), // once
				// for
				// initial
				// build;
				// once
				// for
				// release
				// build
				oneOf(containsString("Building core-utils")), oneOf(containsString("Building console-app")),
				oneOf(containsString("The Calculator Test has run"))));
	}

	private void installsAllModulesIntoTheRepoWithTheBuildNumber() throws Exception {
		assertArtifactInLocalRepo("ch.sourcepond.maven.plugins.testprojects.incrementsnapshotversionafterrelease",
				"increment-snapshot-version-after-release", expectedParentVersion);
		assertArtifactInLocalRepo("ch.sourcepond.maven.plugins.testprojects.incrementsnapshotversionafterrelease",
				"core-utils", expectedCoreVersion);
		assertArtifactInLocalRepo("ch.sourcepond.maven.plugins.testprojects.incrementsnapshotversionafterrelease",
				"console-app", expectedAppVersion);
	}

	private void theLocalAndRemoteGitReposAreTaggedWithTheModuleNameAndVersion()
			throws IOException, InterruptedException {
		assertThat(testProject.local, hasTag("increment-snapshot-version-after-release-" + expectedParentVersion));
		assertThat(testProject.origin, hasTag("increment-snapshot-version-after-release-" + expectedParentVersion));

		assertThat(testProject.local, hasTag("core-utils-" + expectedCoreVersion));
		assertThat(testProject.origin, hasTag("core-utils-" + expectedCoreVersion));

		assertThat(testProject.local, hasTag("console-app-" + expectedAppVersion));
		assertThat(testProject.origin, hasTag("console-app-" + expectedAppVersion));
	}

	@Ignore
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

	@Ignore
	@Test
	public void whenRunFromASubFolderItShowsAnError() throws IOException, InterruptedException {
		try {
			new MvnRunner().runMaven(new File(testProject.localDir, "console-app"), "-DbuildNumber=" + buildNumber,
					"releaser:release");
			Assert.fail("Should not have worked");
		} catch (final MavenExecutionException e) {
			assertThat(e.output, threeOf(
					containsString("The release plugin can only be run from the root folder of your Git repository")));
			assertThat(e.output, oneOf(
					containsString("Try running the release plugin from " + testProject.localDir.getCanonicalPath())));
		}
	}

	// @Test
	// public void
	// whenOneModuleDependsOnAnotherThenWhenReleasingThisDependencyHasTheRelaseVersion()
	// {
	// // TODO: implement this
	// }

	private ObjectId head(final Git git) throws IOException {
		return git.getRepository().getRef("HEAD").getObjectId();
	}
}