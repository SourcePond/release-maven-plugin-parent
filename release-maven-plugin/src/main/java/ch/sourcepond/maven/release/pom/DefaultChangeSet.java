/*Copyright (C) 2016 Roland Hauser, <sourcepond@gmail.com>
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
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
	private ChangeSetCloseException failure;

	DefaultChangeSet(final Log pLog, final SCMRepository pRepository, final MavenXpp3Writer pWriter) {
		log = pLog;
		repository = pRepository;
		writer = pWriter;
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
				repository.pushChanges(modelsToBeIncremented.keySet());
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