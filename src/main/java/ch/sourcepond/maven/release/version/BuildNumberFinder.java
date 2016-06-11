package ch.sourcepond.maven.release.version;

import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.project.MavenProject;

import ch.sourcepond.maven.release.providers.RootProject;
import ch.sourcepond.maven.release.scm.ProposedTag;
import ch.sourcepond.maven.release.scm.SCMException;
import ch.sourcepond.maven.release.scm.SCMRepository;

@Named
@Singleton
class BuildNumberFinder {
	static final String SNAPSHOT_EXTENSION = "-SNAPSHOT";
	private final RootProject rootProject;
	private final SCMRepository repository;

	@Inject
	BuildNumberFinder(final RootProject pRootProject, final SCMRepository pRepository) {
		rootProject = pRootProject;
		repository = pRepository;
	}

	public long findBuildNumber(final MavenProject project, final String businessVersion) throws VersionException {
		final SortedSet<Long> prev = new TreeSet<>();

		try {
			for (final ProposedTag previousTag : repository.tagsForVersion(project.getArtifactId(), businessVersion)) {
				prev.add(previousTag.getBuildNumber());
			}

			prev.addAll(repository.getRemoteBuildNumbers(rootProject.getRemoteUrlOrNull(), project.getArtifactId(),
					businessVersion));
			return prev.isEmpty() ? 0l : prev.last() + 1;
		} catch (final SCMException e) {
			throw new VersionException(e, "Build number could not be determined!");
		}
	}

	public String newBusinessVersion(final MavenProject project, final boolean useLastDigitAsVersionNumber) {
		String businessVersion = project.getVersion().replace(SNAPSHOT_EXTENSION, "");
		if (useLastDigitAsVersionNumber) {
			businessVersion = businessVersion.substring(0, businessVersion.lastIndexOf('.'));
		}
		return businessVersion;
	}
}
