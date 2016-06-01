package ch.sourcepond.maven.release.pom;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.logging.Log;

import ch.sourcepond.maven.release.scm.SCMRepository;

/**
 * @author rolandhauser
 *
 */
class ChangeSetCreator {
	private final Map<File, Model> releases = new LinkedHashMap<>();
	private final Map<File, Model> snapshotVersionIncrements = new LinkedHashMap<>();
	private final Log log;
	private final SCMRepository repository;
	private final MavenXpp3Writer writer;

	ChangeSetCreator(final Log log, final SCMRepository repository, final MavenXpp3Writer writer) {
		this.log = log;
		this.repository = repository;
		this.writer = writer;
	}

	private void registerModel(final Map<File, Model> models, final File file, final Model model)
			throws POMUpdateException {
		try {
			models.put(file.getCanonicalFile(), model);
		} catch (final IOException e) {
			throw new POMUpdateException(e, "Canonical path could be determined for file %s", file);
		}
	}

	void markRelease(final File file, final Model model) throws POMUpdateException {
		registerModel(releases, file, model);
	}

	void markSnapshotVersionIncrement(final File file, final Model model) throws POMUpdateException {
		registerModel(snapshotVersionIncrements, file, model);
	}

	ChangeSet newChangeSet(final String remoteUrlOrNull) throws POMUpdateException {
		final DefaultChangeSet changedFiles = new DefaultChangeSet(log, repository, writer, releases,
				snapshotVersionIncrements, remoteUrlOrNull);

		changedFiles.writeChanges();

		return changedFiles;
	}
}
