package ch.sourcepond.maven.release.pom;

@SuppressWarnings("serial")
public class ChangeSetCloseException extends POMUpdateException {

	public ChangeSetCloseException(final Throwable cause, final String format, final Object... args) {
		super(cause, format, args);
	}

}
