package ch.sourcepond.integrationtest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;

import org.apache.maven.shared.invoker.MavenInvocationException;
import org.junit.Test;

import ch.sourcepond.integrationtest.utils.MvnRunner;
import ch.sourcepond.integrationtest.utils.TestProject;

/**
 * This test actually downloads multiple versions of maven and runs the plugin
 * against them.
 */
public class MavenCompatibilityTest extends E2ETest {

	final TestProject testProject = TestProject.singleModuleProject();


	@Test
	public void maven_3_3_3() throws Exception {
		buildProjectWithMavenVersion("3.3.3");
	}
	
	@Test
	public void maven_3_3_9() throws Exception {
		buildProjectWithMavenVersion("3.3.9");
	}

	private void buildProjectWithMavenVersion(final String mavenVersionToTest)
			throws IOException, InterruptedException, MavenInvocationException {
		final String buildNumber = mavenVersionToTest.replace(".", "") + String.valueOf(System.currentTimeMillis());
		final String expected = "1.0." + buildNumber;
		final MvnRunner runner = mvn.mvn(mavenVersionToTest);
		testProject.setMvnRunner(runner);
		testProject.mvnRelease(buildNumber);
		runner.assertArtifactInLocalRepo("ch.sourcepond.maven.plugins.testprojects", "single-module", expected);
		assertThat(new File(testProject.localDir, "target/single-module-" + expected + "-package.jar").exists(),
				is(true));
	}

}