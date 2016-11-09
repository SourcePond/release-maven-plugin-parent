package ch.sourcepond.maven.release.pom;

import static ch.sourcepond.maven.release.pom.DefaultChangeSet.REVERT_ERROR_MESSAGE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.logging.Log;
import org.junit.Test;

import ch.sourcepond.maven.release.scm.SCMException;
import ch.sourcepond.maven.release.scm.SCMRepository;

public class DefaultChangeSetTest {
	private static final String ANY_MESSAGE = "anyMessage";
	private final Log log = mock(Log.class);
	private final SCMRepository repository = mock(SCMRepository.class);
	private final MavenXpp3Writer writer = mock(MavenXpp3Writer.class);
	private final Map<File, Model> releasedModels = new LinkedHashMap<>();
	private final DefaultChangeSet set = new DefaultChangeSet(log, repository, writer);

	/**
	 * @throws Exception
	 */
	@Test
	public void closeNoFailureSetRevertSuccess() throws Exception {
		set.close();
		verify(repository).revertChanges(releasedModels.keySet());
		verify(log, never()).warn(REVERT_ERROR_MESSAGE);
	}

	@Test
	public void closeFailureSetRevertSuccess() throws Exception {
		final Exception expected = new Exception();
		set.setFailure(ANY_MESSAGE, expected);
		try {
			set.close();
			fail("Exception expected!");
		} catch (final ChangeSetCloseException e) {
			assertEquals(ANY_MESSAGE, e.getMessage());
			assertSame(expected, e.getCause());
		}
		verify(log, never()).warn(REVERT_ERROR_MESSAGE);
	}

	@Test
	public void closeFailureSetRevertFailed() throws Exception {
		final SCMException revertException = new SCMException("any");
		doThrow(revertException).when(repository).revertChanges(releasedModels.keySet());

		final Exception expected = new Exception();
		set.setFailure(ANY_MESSAGE, expected);
		try {
			set.close();
			fail("Exception expected!");
		} catch (final ChangeSetCloseException e) {
			assertEquals(ANY_MESSAGE, e.getMessage());
			assertSame(expected, e.getCause());
		}
		verify(log).warn(REVERT_ERROR_MESSAGE, revertException);
	}

	@Test
	public void closeSCMExceptionOccurred() throws Exception {
		final SCMException expected = new SCMException("any");
		doThrow(expected).when(repository).revertChanges(releasedModels.keySet());
		try {
			set.close();
			fail("Exception expected!");
		} catch (final ChangeSetCloseException e) {
			assertSame(expected, e.getCause());
		}
		verify(log, never()).warn(REVERT_ERROR_MESSAGE);
	}
}
