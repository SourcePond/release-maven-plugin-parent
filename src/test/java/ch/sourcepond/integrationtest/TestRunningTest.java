package ch.sourcepond.integrationtest;

import org.junit.Assert;
import org.junit.Test;

import ch.sourcepond.integrationtest.utils.MavenExecutionException;
import ch.sourcepond.integrationtest.utils.TestProject;

import java.io.IOException;

import static ch.sourcepond.integrationtest.utils.GitMatchers.hasCleanWorkingDirectory;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

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
        assertThat(projectWithTestsThatFail.local.tagList().call().get(0).getName(), is("refs/tags/module-with-test-failure-1.0.1"));
        assertThat(projectWithTestsThatFail.origin.tagList().call().get(0).getName(), is("refs/tags/module-with-test-failure-1.0.1"));
    }

    @Test
    public void ifTestsAreSkippedYouCanReleaseWithoutRunningThem() throws IOException {
        projectWithTestsThatFail.mvn(
            "-DbuildNumber=1", "-DskipTests",
            "releaser:release");
    }

}
