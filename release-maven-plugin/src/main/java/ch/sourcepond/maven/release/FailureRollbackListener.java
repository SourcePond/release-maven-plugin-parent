package ch.sourcepond.maven.release;

import static java.util.Collections.unmodifiableSet;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.execution.MojoExecutionEvent;
import org.apache.maven.execution.MojoExecutionListener;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;

@Named
@Singleton
public class FailureRollbackListener implements MojoExecutionListener {
	static final Set<String> DEPLOY_PLUGINS;
	static {
		final Set<String> deployPlugins = new HashSet<>();
		deployPlugins.add("org.sonatype.plugins:nexus-staging-maven-plugin");
		deployPlugins.add("org.apache.maven.plugins:maven-deploy-plugin");
		DEPLOY_PLUGINS = unmodifiableSet(deployPlugins);
	}
	
	@Override
	public void afterExecutionFailure(MojoExecutionEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterMojoExecutionSuccess(MojoExecutionEvent event) throws MojoExecutionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeMojoExecution(final MojoExecutionEvent event) throws MojoExecutionException {
		final MojoExecution execution = event.getExecution();
		if (DEPLOY_PLUGINS.contains(execution.getPlugin().getKey())) {
			
		}
	}
}
