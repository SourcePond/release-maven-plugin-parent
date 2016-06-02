package ch.sourcepond.maven.release.providers;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Named
@Singleton
final class ReactorProjectsProvider extends BaseProvider<ReactorProjects> {

	@Inject
	ReactorProjectsProvider(final DefaultMavenComponentSingletons pSingletons) {
		super(pSingletons);
	}

	@Override
	protected ReactorProjects getDelegate() {
		return singletons.getReactorProjects();
	}

	@Override
	protected Class<ReactorProjects> getDelegateType() {
		return ReactorProjects.class;
	}

}
