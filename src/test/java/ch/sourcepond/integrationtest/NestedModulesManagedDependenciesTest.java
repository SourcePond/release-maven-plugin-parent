package ch.sourcepond.integrationtest;

import ch.sourcepond.integrationtest.utils.TestProject;

public class NestedModulesManagedDependenciesTest extends NestedModulesBaseTest {

	@Override
	protected TestProject newTestProject() {
		return TestProject.nestedProjectManagedDependencies();
	}

}
