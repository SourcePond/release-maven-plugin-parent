package ch.sourcepond.maven.release.pom;

import static ch.sourcepond.maven.release.pom.UpdateProcessor.DEPENDENCY_ERROR_INTRO;
import static ch.sourcepond.maven.release.pom.UpdateProcessor.DEPENDENCY_ERROR_SUMMARY;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.maven.model.Model;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import ch.sourcepond.maven.release.reactor.Reactor;
import ch.sourcepond.maven.release.reactor.ReleasableModule;
import ch.sourcepond.maven.release.version.Version;

/**
 * @author rolandhauser
 *
 */
public class UpdateProcessorTest {
	private static final String ANY_REMOTE_URL = "anyRemoteUrl";
	private static final String ANY_ARTIFACT_ID = "anyArtifactId";
	private static final String ANY_VERSION = "anyVersion";
	private static final String ANY_ERROR = "anyError";
	private static final File ANY_POM = new File("anyPom");
	private final Log log = mock(Log.class);
	private final Reactor reactor = mock(Reactor.class);
	private final ReleasableModule module = mock(ReleasableModule.class);
	private final ContextFactory contextFactory = mock(ContextFactory.class);
	private final DefaultChangeSetFactory writerFactory = mock(DefaultChangeSetFactory.class);
	private final Context context = mock(Context.class);
	private final Command command = mock(Command.class);
	private final List<Command> commands = asList(command);
	private final MavenProject project = mock(MavenProject.class);
	private final DefaultChangeSet changeSet = mock(DefaultChangeSet.class);
	private final Model originalModel = mock(Model.class);
	private final Model clonedModel = mock(Model.class);
	private final UpdateProcessor processor = new UpdateProcessor(log, contextFactory, writerFactory, commands);;

	@SuppressWarnings("unchecked")
	@Before
	public void setup() throws POMUpdateException {
		// Setup context factory
		when(contextFactory.newContext(reactor, project, clonedModel, false)).thenReturn(context);

		// Setup context
		when(context.getErrors()).thenReturn(Collections.<String> emptyList());
		when(context.getModel()).thenReturn(clonedModel);

		// Setup writer factory
		when(writerFactory.newChangeSet(ANY_REMOTE_URL)).thenReturn(changeSet);

		// Setup writer
		final Map<File, Model> modelsToBeReleased = new LinkedHashMap<>();
		modelsToBeReleased.put(ANY_POM, clonedModel);
		when(changeSet.getModelsToBeReleased()).thenReturn(modelsToBeReleased);
		when(changeSet.writeChanges()).thenReturn(changeSet);

		// Setup reactor
		final Iterator<ReleasableModule> it = mock(Iterator.class);
		when(it.hasNext()).thenReturn(true).thenReturn(false);
		when(it.next()).thenReturn(module).thenThrow(NoSuchElementException.class);
		when(reactor.iterator()).thenReturn(it);

		// Setup module
		final Version version = mock(Version.class);
		when(version.getReleaseVersion()).thenReturn(ANY_VERSION);
		when(module.willBeReleased()).thenReturn(true);
		when(module.getProject()).thenReturn(project);
		when(module.getArtifactId()).thenReturn(ANY_ARTIFACT_ID);
		when(module.getVersion()).thenReturn(version);

		// Setup project
		when(project.clone()).thenReturn(project);
		when(project.getFile()).thenReturn(ANY_POM);
		when(project.getOriginalModel()).thenReturn(originalModel);
		when(originalModel.clone()).thenReturn(clonedModel);
	}

	@Test
	public void updatePomsCompletedSuccessfully() throws Exception {
		final DefaultChangeSet updatedPoms = (DefaultChangeSet) processor.updatePoms(reactor, ANY_REMOTE_URL, false);
		final Iterator<File> it = updatedPoms.getModelsToBeReleased().keySet().iterator();
		assertTrue(it.hasNext());
		assertSame(ANY_POM, it.next());
		assertFalse(it.hasNext());

		final InOrder order = inOrder(clonedModel, command, log, changeSet);
		order.verify(log).info("Going to release anyArtifactId anyVersion");
		order.verify(command).alterModel(context);
		order.verify(changeSet).markRelease(ANY_POM, clonedModel);
	}

	@Test
	public void updatePomsDependencyErrorsOccurred() throws Exception {
		when(context.getErrors()).thenReturn(asList(ANY_ERROR));
		try {
			processor.updatePoms(reactor, ANY_REMOTE_URL, false);
			fail("Exception expected");
		} catch (final POMUpdateException e) {
			assertEquals(DEPENDENCY_ERROR_SUMMARY, e.getMessage());
			final List<String> msgs = e.getMessages();
			assertEquals(2, msgs.size());
			assertEquals(DEPENDENCY_ERROR_INTRO, msgs.get(0));
			assertEquals(" * anyError", msgs.get(1));
		}
	}

	/**
	 * TODO: Check todo-comment in
	 * {@link UpdateProcessor#updatePoms(Log, Reactor)} and rewrite this test if
	 * necessary
	 */
	@Test
	public void updatePomsModuleWillNotBeReleased() throws Exception {
		when(module.willBeReleased()).thenReturn(false);
		final DefaultChangeSet updatedPoms = (DefaultChangeSet) processor.updatePoms(reactor, ANY_REMOTE_URL, false);
		final Iterator<File> it = updatedPoms.getModelsToBeReleased().keySet().iterator();
		assertTrue(it.hasNext());
		assertSame(ANY_POM, it.next());
		assertFalse(it.hasNext());

		final InOrder order = inOrder(clonedModel, command, log, changeSet);

		// This is the only difference between a "normal" run and a run when
		// module#willBeReleased() returns false; see
		// updatePomsCompletedSuccessfully
		order.verify(log, Mockito.never()).info("Going to release anyArtifactId anyVersion");

		order.verify(command).alterModel(context);
		order.verify(changeSet).markRelease(ANY_POM, clonedModel);
	}
}