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

import static java.lang.String.format;
import static java.util.Collections.sort;
import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.model.Model;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import ch.sourcepond.maven.release.commons.Version;
import ch.sourcepond.maven.release.reactor.Reactor;
import ch.sourcepond.maven.release.reactor.ReleasableModule;


@Named
@Singleton
final class UpdateProcessor implements Updater {
	static final String DEPENDENCY_ERROR_SUMMARY = "Cannot release with references to snapshot dependencies";
	static final String DEPENDENCY_ERROR_INTRO = "The following dependency errors were found:";
	private final ContextFactory contextFactory;
	private final DefaultChangeSetFactory changeSetFactory;
	private final Log log;
	private final List<Command> commands;

	@Inject
	UpdateProcessor(final Log pLog, final ContextFactory pContextFactory,
			final DefaultChangeSetFactory pChangeSetFactory, final List<Command> pCommands) {
		log = pLog;
		contextFactory = pContextFactory;
		changeSetFactory = pChangeSetFactory;
		final List<Command> sortedCmds = new ArrayList<>(pCommands);
		sort(sortedCmds);
		commands = unmodifiableList(sortedCmds);
	}

	List<Command> getCommands() {
		return commands;
	}

	private void process(final Context context, final List<String> errors) {
		for (final Command cmd : commands) {
			cmd.alterModel(context);
		}
		errors.addAll(context.getErrors());
	}

	@Override
	public ChangeSet updatePoms(final Reactor reactor, final boolean incrementSnapshotVersionAfterRelease)
			throws POMUpdateException {
		final DefaultChangeSet changeSet = changeSetFactory.newChangeSet();
		final List<String> errors = new LinkedList<String>();

		for (final ReleasableModule module : reactor) {
			final Version version = module.getVersion();

			// TODO: If a module will not be released, is further processing
			// necessary or should we continue the loop here?
			if (module.getVersion().hasChanged()) {
				log.info(format("Going to release %s %s", module.getArtifactId(), version.getReleaseVersion()));
			}

			final MavenProject project = module.getProject();
			final Model releaseModel = project.getOriginalModel().clone();
			Context context = contextFactory.newContext(reactor, project, releaseModel, false);
			process(context, errors);
			// Mark project to be written at a later stage; if an exception
			// occurs, we don't need to revert anything.
			changeSet.markRelease(project.getFile(), releaseModel, context.needsOwnVersion());

			if (incrementSnapshotVersionAfterRelease) {
				final Model snapshotModel = project.getOriginalModel().clone();
				context = contextFactory.newContext(reactor, project, snapshotModel, true);
				process(context, errors);
				changeSet.markSnapshotVersionIncrement(project.getFile(), snapshotModel, context.needsOwnVersion());
			}
		}

		if (!errors.isEmpty()) {
			final POMUpdateException exception = new POMUpdateException(DEPENDENCY_ERROR_SUMMARY);
			exception.add(DEPENDENCY_ERROR_INTRO);
			for (final String dependencyError : errors) {
				exception.add(" * %s", dependencyError);
			}
			throw exception;
		}

		// At this point it's guaranteed that no dependency errors occurred.
		return changeSet.writeChanges();
	}
}