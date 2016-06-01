package ch.sourcepond.maven.release.pom;

import static ch.sourcepond.maven.release.pom.DefaultChangeSet.REVERT_ERROR_MESSAGE;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.logging.Log;

import ch.sourcepond.maven.release.scm.SCMException;
import ch.sourcepond.maven.release.scm.SCMRepository;

@SuppressWarnings("serial")
class SnapshotIncrementChangeSet implements AutoCloseable {
	static final String IO_EXCEPTION_FORMAT = "Updated project %s could not be written!";
	static final String PUSH_EXCEPTION_FORMAT = "Changed files %s could not be pushed";
	private final Map<File, Model> changed;
	private final Log log;
	private final SCMRepository repository;
	private final MavenXpp3Writer pomWriter;
	private final String remoteUrlOrNull;

	SnapshotIncrementChangeSet(final Log log, final SCMRepository repository, final MavenXpp3Writer pomWriter,
			final Map<File, Model> changed, final String remoteUrlOrNull) {
		this.log = log;
		this.repository = repository;
		this.pomWriter = pomWriter;
		this.changed = changed;
		this.remoteUrlOrNull = remoteUrlOrNull;
	}

	@Override
	public void close() throws ChangeSetCloseException {
		if (!changed.isEmpty()) {
			final List<File> changedFiles = new LinkedList<>();
			for (final Map.Entry<File, Model> entry : changed.entrySet()) {
				try {
					// It's necessary to use the canonical file here, otherwise
					// GIT
					// revert can fail when symbolic links are used (ends up in
					// an
					// empty path and revert fails).
					final File changedFile = entry.getKey().getCanonicalFile();
					changedFiles.add(changedFile);
					try (final Writer fileWriter = new FileWriter(changedFile)) {
						pomWriter.write(fileWriter, entry.getValue());
					}
				} catch (final IOException e) {
					try {
						repository.revertChanges(changedFiles);
					} catch (final SCMException revertException) {
						// warn if you can't revert but keep throwing the
						// original
						// exception so the root cause isn't lost
						log.warn(REVERT_ERROR_MESSAGE, e);
					}
					throw new ChangeSetCloseException(e, IO_EXCEPTION_FORMAT, entry.getValue());
				}
			}
			try {
				repository.pushChanges(remoteUrlOrNull);
			} catch (final SCMException e) {
				throw new ChangeSetCloseException(e, IO_EXCEPTION_FORMAT, changedFiles);
			}
		}
	}
}