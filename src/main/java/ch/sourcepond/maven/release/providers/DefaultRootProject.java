package ch.sourcepond.maven.release.providers;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.lib.Repository;

import ch.sourcepond.maven.release.reactor.ReactorException;

@Named
class DefaultRootProject implements RootProject {
	private final DefaultMavenComponentSingletons singletons;

	@Inject
	DefaultRootProject(final DefaultMavenComponentSingletons pSingletons) {
		singletons = pSingletons;
	}

	private MavenProject getProject() {
		return singletons.getProject();
	}

	@Override
	public Collection<String> getActiveProfileIds() {
		final List<String> ids = new LinkedList<>();
		for (final Object activatedProfile : getProject().getActiveProfiles()) {
			ids.add(((org.apache.maven.model.Profile) activatedProfile).getId());
		}
		return ids;
	}

	@Override
	public String calculateModulePath(final MavenProject project) throws ReactorException {
		// Getting canonical files because on Windows, it's possible one returns
		// "C:\..." and the other "c:\..." which is rather amazing
		File projectRoot;
		File moduleRoot;
		try {
			projectRoot = getProject().getBasedir().getCanonicalFile();
			moduleRoot = project.getBasedir().getCanonicalFile();
		} catch (final IOException e) {
			throw new ReactorException(e, "Could not find directory paths for maven project");
		}
		String relativePathToModule = Repository.stripWorkDir(projectRoot, moduleRoot);
		if (relativePathToModule.length() == 0) {
			relativePathToModule = ".";
		}
		return relativePathToModule;
	}
}
