package ch.sourcepond.maven.release.reactor;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import ch.sourcepond.maven.release.providers.BaseProvider;
import ch.sourcepond.maven.release.providers.MavenComponentSingletons;

@Named
@Singleton
final class ReactorProjectsProvider extends BaseProvider<ReactorProjects> {

	@Inject
	ReactorProjectsProvider(final MavenComponentSingletons pSingletons) {
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
