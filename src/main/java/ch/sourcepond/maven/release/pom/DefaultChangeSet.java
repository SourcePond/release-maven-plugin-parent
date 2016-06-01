package ch.sourcepond.maven.release.pom;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.logging.Log;

import ch.sourcepond.maven.release.scm.SCMException;
import ch.sourcepond.maven.release.scm.SCMRepository;

/**
 * Default implementation of the {@link ChangeSet} interface.
 *
 */
final class DefaultChangeSet implements ChangeSet {
	static final String EXCEPTION_MESSAGE = "Unexpected exception while setting the release versions in the pom";
	static final String REVERT_ERROR_MESSAGE = "Could not revert changes - working directory is no longer clean. Please revert changes manually";
	private final Log log;
	private final SCMRepository repository;
	private final SnapshotIncrementChangeSet snapshotIncrementChangeSet;
	private final Map<File, Model> changes;
	private final MavenXpp3Writer writer;
	private ChangeSetCloseException failure;

	DefaultChangeSet(final Log log, final SCMRepository repository,
			final SnapshotIncrementChangeSet snapshotIncrementChangeSet, final MavenXpp3Writer writer,
			final Map<File, Model> changes) {
		this.log = log;
		this.repository = repository;
		this.snapshotIncrementChangeSet = snapshotIncrementChangeSet;
		this.writer = writer;
		this.changes = changes;
	}

	public void writeChanges() throws POMUpdateException {
		for (final Map.Entry<File, Model> entry : changes.entrySet()) {
			try {
				try (final Writer fileWriter = new FileWriter(entry.getKey())) {
					writer.write(fileWriter, entry.getValue());
				}
			} catch (final IOException e) {
				setFailure(EXCEPTION_MESSAGE, e);
				close();
			}
		}
	}

	@Override
	public void close() throws ChangeSetCloseException {
		try {
			repository.revertChanges(changes.keySet());
		} catch (final SCMException e) {
			if (failure == null) {
				// throw if you can't revert as that is the root problem
				throw new ChangeSetCloseException(e, REVERT_ERROR_MESSAGE);
			} else {
				// warn if you can't revert but keep throwing the original
				// exception so the root cause isn't lost
				log.warn(REVERT_ERROR_MESSAGE, e);
			}
		}
		if (failure != null) {
			log.info("Reverted changes because there was an error.");
			throw failure;
		}

		snapshotIncrementChangeSet.close();
	}

	@Override
	public void setFailure(final String message, final Exception failure) {
		this.failure = new ChangeSetCloseException(failure, message);
	}

	@Override
	public Iterator<File> iterator() {
		return changes.keySet().iterator();
	}
}
