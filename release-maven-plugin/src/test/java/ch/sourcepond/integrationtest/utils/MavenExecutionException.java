package ch.sourcepond.integrationtest.utils;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

public class MavenExecutionException extends RuntimeException {
	public final int exitCode;
	public final List<String> output;

	public MavenExecutionException(final int exitCode, final List<String> output) {
		super("Error from mvn: " + output);
		this.exitCode = exitCode;
		this.output = output;
	}

	@Override
	public String toString() {
		return "MavenExecutionException{" + "exitCode=" + exitCode + ", output="
				+ StringUtils.join(output.toArray(), SystemUtils.LINE_SEPARATOR);
	}
}
