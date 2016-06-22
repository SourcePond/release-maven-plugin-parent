package ch.sourcepond.integrationtest;

import ch.sourcepond.integrationtest.utils.TestProject;

public class NestedModulesTest extends NestedModulesBaseTest {

	@Override
	protected TestProject newTestProject() {
		return TestProject.nestedProject();
	}

}
