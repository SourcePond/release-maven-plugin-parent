package ch.sourcepond.integrationtest;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Test;

import ch.sourcepond.integrationtest.utils.TestProject;

import java.io.IOException;
import java.util.List;

import static ch.sourcepond.integrationtest.utils.ExactCountMatcher.oneOf;
import static ch.sourcepond.integrationtest.utils.ExactCountMatcher.twoOf;
import static ch.sourcepond.integrationtest.utils.GitMatchers.hasCleanWorkingDirectory;
import static ch.sourcepond.integrationtest.utils.GitMatchers.hasTag;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class InheritedVersionsTest extends E2ETest {

    public static final String[] ARTIFACT_IDS = new String[]{"inherited-versions-from-parent", "core-utils", "console-app"};
    final String buildNumber = String.valueOf(System.currentTimeMillis());
    final String expected = "1.0." + buildNumber;
    final TestProject testProject = TestProject.inheritedVersionsFromParent();

    @Test
    public void buildsAndInstallsAndTagsAllModules() throws Exception {
        buildsEachProjectOnceAndOnlyOnce(testProject.mvnRelease(buildNumber));
        installsAllModulesIntoTheRepoWithTheBuildNumber();
        theLocalAndRemoteGitReposAreTaggedWithTheModuleNameAndVersion();
    }

    private void buildsEachProjectOnceAndOnlyOnce(List<String> commandOutput) throws Exception {
        assertThat(
            commandOutput,
            allOf(
                oneOf(containsString("Going to release inherited-versions-from-parent " + expected)),
                twoOf(containsString("Building inherited-versions-from-parent")), // once for initial build; once for release build
                oneOf(containsString("Building core-utils")),
                oneOf(containsString("Building console-app")),
                oneOf(containsString("The Calculator Test has run"))
            )
        );
    }

    private void installsAllModulesIntoTheRepoWithTheBuildNumber() throws Exception {
        assertArtifactInLocalRepo("ch.sourcepond.maven.plugins.testprojects.versioninheritor", "inherited-versions-from-parent", expected);
        assertArtifactInLocalRepo("ch.sourcepond.maven.plugins.testprojects.versioninheritor", "core-utils", expected);
        assertArtifactInLocalRepo("ch.sourcepond.maven.plugins.testprojects.versioninheritor", "console-app", expected);
    }

    private void theLocalAndRemoteGitReposAreTaggedWithTheModuleNameAndVersion() throws IOException, InterruptedException {
        for (String artifactId : ARTIFACT_IDS) {
            String expectedTag = artifactId + "-" + expected;
            assertThat(testProject.local, hasTag(expectedTag));
            assertThat(testProject.origin, hasTag(expectedTag));
        }
    }

    @Test
    public void thePomChangesAreRevertedAfterTheRelease() throws IOException, InterruptedException {
        ObjectId originHeadAtStart = head(testProject.origin);
        ObjectId localHeadAtStart = head(testProject.local);
        assertThat(originHeadAtStart, equalTo(localHeadAtStart));
        testProject.mvnRelease(buildNumber);
        assertThat(head(testProject.origin), equalTo(originHeadAtStart));
        assertThat(head(testProject.local), equalTo(localHeadAtStart));
        assertThat(testProject.local, hasCleanWorkingDirectory());
    }

//    @Test
//    public void whenOneModuleDependsOnAnotherThenWhenReleasingThisDependencyHasTheRelaseVersion() {
//        // TODO: implement this
//    }

    private ObjectId head(Git git) throws IOException {
        return git.getRepository().getRef("HEAD").getObjectId();
    }
}
