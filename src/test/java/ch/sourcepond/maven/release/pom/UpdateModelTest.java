package ch.sourcepond.maven.release.pom;

import static ch.sourcepond.maven.release.pom.UpdateModel.ERROR_FORMAT;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;

import ch.sourcepond.maven.release.reactor.UnresolvedSnapshotDependencyException;

/**
 * @author rolandhauser
 *
 */
public class UpdateModelTest {
	private static final String GROUP_ID = "groupId";
	private static final String ARTIFACT_ID = "artifactId";
	private static final String VERSION = "version";
	private final MavenProject project = mock(MavenProject.class);
	private final Model model = mock(Model.class);
	private final Parent parent = mock(Parent.class);
	private final Context context = mock(Context.class);
	private final UpdateModel update = new UpdateModel(mock(Log.class));

	@Before
	public void setup() throws Exception {
		when(project.getGroupId()).thenReturn(GROUP_ID);
		when(project.getArtifactId()).thenReturn(ARTIFACT_ID);
		when(project.getOriginalModel()).thenReturn(model);
		when(context.getModel()).thenReturn(model);
		when(context.getProject()).thenReturn(project);
		when(context.getVersionToDependOn(GROUP_ID, ARTIFACT_ID)).thenReturn(VERSION);
		when(model.getParent()).thenReturn(parent);
	}

	@Test
	public void verifyNeedsOwnVersion() {
		when(model.getVersion()).thenReturn(null);
		when(context.hasNotChanged(parent)).thenReturn(true);
		when(context.dependencyUpdated()).thenReturn(true);
		update.alterModel(context);
		verify(model, times(2)).getVersion();
		verify(model).setVersion(VERSION);
	}

	@Test
	public void verifyAlterModel() throws Exception {
		when(model.getVersion()).thenReturn(VERSION);
		update.alterModel(context);
		verify(model, times(2)).getVersion();
		verify(model).setVersion(VERSION);
	}

	@Test
	public void verifyAlterModelProjectNotFound() throws Exception {
		when(model.getVersion()).thenReturn(VERSION);
		final UnresolvedSnapshotDependencyException expected = new UnresolvedSnapshotDependencyException(GROUP_ID,
				ARTIFACT_ID);
		doThrow(expected).when(context).getVersionToDependOn(GROUP_ID, ARTIFACT_ID);
		update.alterModel(context);
		verify(model, never()).setVersion(VERSION);
		verify(context).addError(ERROR_FORMAT, project);
	}

	@Test
	public void verifyAlterModel_NoVersionOnModelSet_ParentUpdated() throws Exception {
		when(context.hasNotChanged(parent)).thenReturn(true);
		when(model.getVersion()).thenReturn(null);
		update.alterModel(context);
		verify(model, times(2)).getVersion();
		verify(model, never()).setVersion(VERSION);
	}

	@Test
	public void verifyAlterModelProjectNotFound_NoVersionOnModelSet() throws Exception {
		when(context.hasNotChanged(parent)).thenReturn(true);
		final UnresolvedSnapshotDependencyException expected = new UnresolvedSnapshotDependencyException(GROUP_ID,
				ARTIFACT_ID);
		doThrow(expected).when(context).getVersionToDependOn(GROUP_ID, ARTIFACT_ID);
		update.alterModel(context);
		verify(model, never()).setVersion(VERSION);
		verify(context, never()).addError(ERROR_FORMAT, project);
	}

}