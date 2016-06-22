package ch.sourcepond.integrationtest;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.shared.invoker.MavenInvocationException;
import org.junit.BeforeClass;

import ch.sourcepond.integrationtest.utils.MvnRunner;

public class E2ETest {
	public static MvnRunner mvn;
	
	@BeforeClass
	public static void createRunner() throws IOException, MavenInvocationException {
		mvn = new MvnRunner();
	}
	
	public static List<String> runMaven(File workingDir, String... arguments) throws IOException {
		return mvn.runMaven(workingDir, arguments);
	}
	
	public static void assertArtifactInLocalRepo(String groupId, String artifactId, String version)
			throws IOException, MavenInvocationException {
		mvn.assertArtifactInLocalRepo(groupId, artifactId, version);
	}
}
