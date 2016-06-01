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
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import ch.sourcepond.maven.release.scm.SCMRepository;

/**
 * @author rolandhauser
 *
 */
@Component(role = PomWriter.class)
class PomWriter {
	static final String EXCEPTION_MESSAGE = "Unexpected exception while setting the release versions in the pom";
	private final Map<File, Model> releases = new LinkedHashMap<>();
	private final Map<File, Model> snapshotVersionIncrements = new LinkedHashMap<>();

	@Requirement(role = SCMRepository.class)
	private SCMRepository repository;

	@Requirement(role = MavenXpp3Writer.class)
	private MavenXpp3Writer writer;

	@Requirement(role = Log.class)
	private Log log;

	void setRepository(final SCMRepository repository) {
		this.repository = repository;
	}

	void setWriter(final MavenXpp3Writer writer) {
		this.writer = writer;
	}

	void setLog(final Log log) {
		this.log = log;
	}

	void markRelease(final File file, final Model model) {
		releases.put(file, model);
	}

	void markSnapshotVersionIncrement(final File file, final Model model) {
		snapshotVersionIncrements.put(file, model);
	}

	ChangeSet writePoms(final String remoteUrl) throws POMUpdateException {
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
