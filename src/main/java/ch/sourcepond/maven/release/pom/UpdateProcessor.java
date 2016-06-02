package ch.sourcepond.maven.release.pom;

import static java.lang.String.format;

import java.util.LinkedList;
import java.util.List;

import org.apache.maven.model.Model;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import ch.sourcepond.maven.release.reactor.Reactor;
import ch.sourcepond.maven.release.reactor.ReleasableModule;
import ch.sourcepond.maven.release.version.Version;

@Component(role = Updater.class)
final class UpdateProcessor implements Updater {
	static final String DEPENDENCY_ERROR_SUMMARY = "Cannot release with references to snapshot dependencies";
	static final String DEPENDENCY_ERROR_INTRO = "The following dependency errors were found:";

	@Requirement(role = ContextFactory.class)
	private ContextFactory contextFactory;

	@Requirement(role = ChangeSetCreatorFactory.class)
	private ChangeSetCreatorFactory creatorFactory;

	@Requirement(role = Log.class)
	private Log log;

	@Requirement(role = Command.class)
	private List<Command> commands;

	void setCommands(final List<Command> commands) {
		this.commands = commands;
	}

	void setCreatorFactory(final ChangeSetCreatorFactory creatorFactory) {
		this.creatorFactory = creatorFactory;
	}

	void setContextFactory(final ContextFactory contextFactory) {
		this.contextFactory = contextFactory;
	}

	void setLog(final Log log) {
		this.log = log;
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
		final DefaultChangeSet changeSet = creatorFactory.newChangeSet(remoteUrl);
		final List<String> errors = new LinkedList<String>();

		for (final ReleasableModule module : reactor) {
			final Version version = module.getVersion();

			// TODO: If a module will not be released, is further processing
			// necessary or should we continue the loop here?
			if (module.willBeReleased()) {
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