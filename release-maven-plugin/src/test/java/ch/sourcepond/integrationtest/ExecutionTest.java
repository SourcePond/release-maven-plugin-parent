package ch.sourcepond.integrationtest;

import org.junit.Test;

import ch.sourcepond.integrationtest.utils.TestProject;

import java.util.List;

import static ch.sourcepond.integrationtest.utils.ExactCountMatcher.noneOf;
import static ch.sourcepond.integrationtest.utils.ExactCountMatcher.oneOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class ExecutionTest extends E2ETest{

    final TestProject testProject = TestProject.moduleWithProfilesProject();
    final String echoPluginOutput = "echo-maven-plugin running because profileActivatedByReleasePlugin is activated";

    @Test
    public void profilesNotPassedToTheReleaseExecutionAreNotPassedOnToTheDeploymentButConfiguredProfilesAre() throws Exception {
        List<String> consoleOutput = testProject.mvnRelease("1");
        assertThat(consoleOutput, noneOf(containsString("The module-with-profiles test has run")));
        assertThat(consoleOutput, oneOf(containsString(echoPluginOutput)));
    }

    @Test
    public void profilesPassedToTheReleaseExecutionArePassedOnToTheDeployment() throws Exception {
        List<String> consoleOutput = testProject.mvn("-DbuildNumber=1", "releaser:release", "-P runTestsProfile");
        assertThat(consoleOutput, oneOf(containsString("The module-with-profiles test has run")));
        assertThat(consoleOutput, oneOf(containsString(echoPluginOutput)));
    }


}
