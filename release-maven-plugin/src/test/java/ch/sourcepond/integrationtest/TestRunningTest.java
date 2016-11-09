package ch.sourcepond.integrationtest;

import static ch.sourcepond.integrationtest.utils.GitMatchers.hasCleanWorkingDirectory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import ch.sourcepond.integrationtest.utils.MavenExecutionException;
import ch.sourcepond.integrationtest.utils.TestProject;

public class TestRunningTest extends E2ETest {
    final TestProject projectWithTestsThatFail = TestProject.moduleWithTestFailure();

    @Test
    public void doesNotReleaseIfThereAreTestFailuresButTagsAreStillWritten() throws Exception {
        try {
            projectWithTestsThatFail.mvnRelease("1");
            Assert.fail("Should have failed");
        } catch (MavenExecutionException e) {

        }
        assertThat(projectWithTestsThatFail.local, hasCleanWorkingDirectory());
        assertTrue(projectWithTestsThatFail.local.tagList().call().isEmpty());
        assertTrue(projectWithTestsThatFail.origin.tagList().call().isEmpty());
    }

    @Test
    public void ifTestsAreSkippedYouCanReleaseWithoutRunningThem() throws IOException {
        projectWithTestsThatFail.mvn(
            "-DbuildNumber=1", "-DskipTests",
            "releaser:release");
    }

}
