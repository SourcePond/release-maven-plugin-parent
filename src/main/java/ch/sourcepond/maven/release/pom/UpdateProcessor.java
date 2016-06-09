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

import ch.sourcepond.maven.release.reactor.Reactor;
import ch.sourcepond.maven.release.reactor.ReleasableModule;
import ch.sourcepond.maven.release.version.Version;

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
	public ChangeSet updatePoms(final Reactor reactor, final String remoteUrl,
			final boolean incrementSnapshotVersionAfterRelease) throws POMUpdateException {
		final DefaultChangeSet changeSet = changeSetFactory.newChangeSet(remoteUrl);
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
			process(contextFactory.newContext(reactor, project, releaseModel, false), errors);
			// Mark project to be written at a later stage; if an exception
			// occurs, we don't need to revert anything.
			changeSet.markRelease(project.getFile(), releaseModel);

			if (incrementSnapshotVersionAfterRelease) {
				final Model snapshotModel = project.getOriginalModel().clone();
				process(contextFactory.newContext(reactor, project, snapshotModel, true), errors);
				changeSet.markSnapshotVersionIncrement(project.getFile(), snapshotModel);
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