package ch.sourcepond.maven.release.pom;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.logging.Log;

import ch.sourcepond.maven.release.substitution.VersionSubstitution;

/**
 * @author rolandhauser
 *
 */
@Named("UpdateManagedDependencies")
@Singleton
final class UpdateManagedDependencies extends UpdateDependencies {

	@Inject
	UpdateManagedDependencies(final Log log, final VersionSubstitution pSubstitution) {
		super(log, pSubstitution);
	}

	@Override
	protected List<Dependency> determineDependencies(final Model model) {
		List<Dependency> dependencies = Collections.emptyList();
		final DependencyManagement mgmt = model.getDependencyManagement();

		if (mgmt != null) {
			dependencies = mgmt.getDependencies();
		}
		return dependencies;
	}
}
