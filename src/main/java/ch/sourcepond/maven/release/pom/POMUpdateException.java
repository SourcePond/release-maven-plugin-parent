package ch.sourcepond.maven.release.pom;

import ch.sourcepond.maven.release.PluginException;

@SuppressWarnings("serial")
public class POMUpdateException extends PluginException {

	public POMUpdateException(final String format, final Object... args) {
		super(format, args);
	}

	public POMUpdateException(final Throwable cause, final String format, final Object... args) {
		super(cause, format, args);
	}

	@Override
	public PluginException add(final String format, final Object... args) {
		super.add(format, args);
		return this;
	}

}
