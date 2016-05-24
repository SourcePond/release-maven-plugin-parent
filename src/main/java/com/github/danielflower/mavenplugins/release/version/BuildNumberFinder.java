package com.github.danielflower.mavenplugins.release.version;

import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import com.github.danielflower.mavenplugins.release.scm.ProposedTag;
import com.github.danielflower.mavenplugins.release.scm.SCMException;
import com.github.danielflower.mavenplugins.release.scm.SCMRepository;

@Component(role = BuildNumberFinder.class)
class BuildNumberFinder {

	@Requirement(role = SCMRepository.class)
	private SCMRepository repository;

	void setRepository(final SCMRepository repository) {
		this.repository = repository;
	}

	public long findBuildNumber(final MavenProject project, final String remoteUrl, final String businessVersion)
			throws VersionException {
		final SortedSet<Long> prev = new TreeSet<>();

		try {
			for (final ProposedTag previousTag : repository.tagsForVersion(project.getArtifactId(), businessVersion)) {
				prev.add(previousTag.buildNumber());
			}

			prev.addAll(repository.getRemoteBuildNumbers(remoteUrl, project.getArtifactId(), businessVersion));
			return prev.isEmpty() ? 0l : prev.last() + 1;
		} catch (final SCMException e) {
			throw new VersionException(e, "Build number could not be determined!");
		}
	}
}