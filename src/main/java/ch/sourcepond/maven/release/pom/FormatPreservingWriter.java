package ch.sourcepond.maven.release.pom;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class FormatPreservingWriter extends StringWriter {
	private static final Pattern VERSION_PATTERN = Pattern.compile("<version>\\s*(.*?)\\s*<\\/version>");
	private final StringBuilder original = new StringBuilder();
	private final File file;

	public FormatPreservingWriter(final File file) throws IOException {
		this.file = file;
		final char[] buffer = new char[1024];
		try (final Reader rd = new BufferedReader(new FileReader(file))) {
			int readChars = rd.read(buffer);
			while (readChars != -1) {
				original.append(buffer, 0, readChars);
				readChars = rd.read(buffer);
			}
		}
	}

	@Override
	public void close() throws IOException {
		final Matcher matcher = VERSION_PATTERN.matcher(toString());
		final Matcher originalMatcher = VERSION_PATTERN.matcher(original);
		int originalIdx = 0;
		int startIdx = 0;

		while (matcher.find() && originalMatcher.find(originalIdx)) {
			final String newVersion = matcher.group(1);
			startIdx = originalMatcher.start(1);
			original.replace(startIdx, originalMatcher.end(1), newVersion);
			originalIdx = startIdx + newVersion.length();
		}

		try (final Writer writer = new BufferedWriter(new FileWriter(file))) {
			writer.write(original.toString());
		}
	}
}
