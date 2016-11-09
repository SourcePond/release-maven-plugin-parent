package ch.sourcepond.integrationtest;

import ch.sourcepond.integrationtest.utils.TestProject;

public class NestedModulesVersionSubstitutionTest extends NestedModulesBaseTest {

	@Override
	protected TestProject newTestProject() throws Exception {
		final TestProject project = TestProject.nestedProjectVersionSubstitution();
		project.mvn("install");
		return project;
	}

}