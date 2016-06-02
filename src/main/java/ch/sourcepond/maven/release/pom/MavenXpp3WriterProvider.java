package ch.sourcepond.maven.release.pom;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.model.io.xpp3.MavenXpp3Writer;

import com.google.inject.Provider;

@Named
@Singleton
class MavenXpp3WriterProvider implements Provider<MavenXpp3Writer> {

	@Singleton
	@Override
	public MavenXpp3Writer get() {
		return new MavenXpp3Writer();
	}
}
