package ch.sourcepond.maven.release.pom;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
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
	static final String EXCEPTION_MESSAGE = "Unexpected exception while setting the release versions in the pom";
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

	void markRelease(final File file, final Model model) {
		releases.put(file, model);
	}

	void markSnapshotVersionIncrement(final File file, final Model model) {
		snapshotVersionIncrements.put(file, model);
	}

	ChangeSet newChangeSet(final String remoteUrl) throws POMUpdateException {
		final SnapshotIncrementChangeSet snapshotIncrementChangeSet = new SnapshotIncrementChangeSet(log, repository,
				writer, snapshotVersionIncrements, remoteUrl);
		final DefaultChangeSet changedFiles = new DefaultChangeSet(log, repository, snapshotIncrementChangeSet);
		try {
			for (final Map.Entry<File, Model> entry : releases.entrySet()) {
				// It's necessary to use the canonical file here, otherwise GIT
				// revert can fail when symbolic links are used (ends up in an
				// empty path and revert fails).
				final File changedFile = entry.getKey().getCanonicalFile();
				changedFiles.add(changedFile);
				try (final Writer fileWriter = new FileWriter(changedFile)) {
					writer.write(fileWriter, entry.getValue());
				}
			}
		} catch (final IOException e) {
			changedFiles.setFailure(EXCEPTION_MESSAGE, e);
			changedFiles.close();
		}

		return changedFiles;
	}
}
