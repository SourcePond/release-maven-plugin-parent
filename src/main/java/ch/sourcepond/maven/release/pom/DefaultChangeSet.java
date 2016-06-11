package ch.sourcepond.maven.release.pom;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.logging.Log;

import com.ximpleware.ModifyException;
import com.ximpleware.NavException;
import com.ximpleware.TranscodeException;

import ch.sourcepond.maven.release.scm.SCMException;
import ch.sourcepond.maven.release.scm.SCMRepository;

/**
 * Default implementation of the {@link ChangeSet} interface.
 *
 */
class DefaultChangeSet implements ChangeSet {
	static final String EXCEPTION_MESSAGE = "Unexpected exception while setting the release versions in the pom";
	static final String REVERT_ERROR_MESSAGE = "Could not revert modelsToBeReleased - working directory is no longer clean. Please revert modelsToBeReleased manually";
	static final String IO_EXCEPTION_FORMAT = "Updated project %s could not be written!";
	private final Log log;
	private final SCMRepository repository;
	private final Map<File, Model> modelsToBeReleased = new LinkedHashMap<>();
	private final Map<File, Model> modelsToBeIncremented = new LinkedHashMap<>();
	private final Set<Model> needOwnVersion = new HashSet<>();
	private final MavenXpp3Writer writer;
	private final String remoteUrlOrNull;
	private ChangeSetCloseException failure;

	DefaultChangeSet(final Log log, final SCMRepository repository, final MavenXpp3Writer writer,
			final String remoteUrlOrNull) {
		this.log = log;
		this.repository = repository;
		this.writer = writer;
		this.remoteUrlOrNull = remoteUrlOrNull;
	}

	Map<File, Model> getModelsToBeReleased() {
		return modelsToBeReleased;
	}

	private void registerModel(final Map<File, Model> models, final File file, final Model model,
			final boolean needsOwnVersion) throws POMUpdateException {
		try {
			models.put(file.getCanonicalFile(), model);

			if (needsOwnVersion) {
				needOwnVersion.add(model);
			}
		} catch (final IOException e) {
			throw new POMUpdateException(e, "Canonical path could be determined for file %s", file);
		}
	}

	void markRelease(final File file, final Model model, final boolean needsOwnVersion) throws POMUpdateException {
		registerModel(modelsToBeReleased, file, model, needsOwnVersion);
	}

	void markSnapshotVersionIncrement(final File file, final Model model, final boolean needsOwnVersion)
			throws POMUpdateException {
		registerModel(modelsToBeIncremented, file, model, needsOwnVersion);
	}

	DefaultChangeSet writeChanges() throws POMUpdateException {
		for (final Map.Entry<File, Model> entry : modelsToBeReleased.entrySet()) {
			try (final Writer fileWriter = new FileWriter(entry.getKey())) {
				writer.write(fileWriter, entry.getValue());
			} catch (final IOException e) {
				setFailure(EXCEPTION_MESSAGE, e);
				close();
			}
		}
		return this;
	}

	private String getOwnVersionOrNull(final Model model) {
		if (needOwnVersion.contains(model)) {
			return model.getVersion();
		}
		return null;
	}

	@Override
	public void close() throws ChangeSetCloseException {
		try {
			repository.revertChanges(modelsToBeReleased.keySet());
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

		if (!modelsToBeIncremented.isEmpty()) {
			for (final Map.Entry<File, Model> entry : modelsToBeIncremented.entrySet()) {
				try (final Writer fileWriter = new VersionTransferWriter(entry.getKey(),
						getOwnVersionOrNull(entry.getValue()))) {
					writer.write(fileWriter, entry.getValue());
				} catch (final IOException | ModifyException | NavException | TranscodeException e) {
					try {
						repository.revertChanges(modelsToBeIncremented.keySet());
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
				repository.pushChanges(remoteUrlOrNull, modelsToBeIncremented.keySet());
			} catch (final SCMException e) {
				throw new ChangeSetCloseException(e, IO_EXCEPTION_FORMAT, modelsToBeIncremented.keySet());
			}
		}
	}

	@Override
	public void setFailure(final String message, final Exception failure) {
		this.failure = new ChangeSetCloseException(failure, message);
	}
}