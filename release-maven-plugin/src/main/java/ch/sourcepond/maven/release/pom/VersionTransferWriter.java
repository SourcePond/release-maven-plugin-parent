/*Copyright (C) 2016 Roland Hauser, <sourcepond@gmail.com>
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package ch.sourcepond.maven.release.pom;

import static com.ximpleware.VTDNav.FC;
import static com.ximpleware.VTDNav.WS_LEADING;
import static java.lang.String.format;
import static org.apache.commons.lang3.Validate.notNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ximpleware.ModifyException;
import com.ximpleware.NavException;
import com.ximpleware.TranscodeException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;

/**
 * Writer which stores all passed data into a buffer. After all data has been
 * written and {@link #close()} has been called, all <em>&lt;version&gt;</em>
 * tags of the original file will be updated with the data actually written. The
 * format of of the original file will be preserved.
 *
 */
final class VersionTransferWriter extends StringWriter {
	static final Pattern VERSION_PATTERN = Pattern.compile("<version>\\s*(.*?)\\s*<\\/version>");
	static final int VERSION_VALUE = 1;
	static final String ARTIFACT_ID = "artifactId";
	static final String ARTIFACT_ID_PATTERN = "<artifactId>\\s*(.*?)\\s*<\\/artifactId>";
	static final String VERSION_FORMAT = "<version>%s</version>";
	private final StringBuilder original = new StringBuilder();
	private final File file;

	/**
	 * Constructs a new instance of this class. During construction, the file
	 * specified will be read and stored in a buffer.
	 * 
	 * @param file
	 *            File to be read, must not be {@code null}
	 * @throws IOException
	 * @throws TranscodeException
	 * @throws NavException
	 * @throws ModifyException
	 */
	public VersionTransferWriter(final File pFile, final String pOwnVersionOrNull)
			throws IOException, ModifyException, NavException, TranscodeException {
		notNull(pFile, "File specified is null");
		file = pFile;
		insertVersionIntoOriginalIfNecessary(pOwnVersionOrNull);

		final char[] buffer = new char[1024];
		try (final Reader rd = new BufferedReader(new FileReader(file))) {
			int readChars = rd.read(buffer);
			while (readChars != -1) {
				original.append(buffer, 0, readChars);
				readChars = rd.read(buffer);
			}
		}

	}

	private void insertVersionIntoOriginalIfNecessary(final String pOwnVersionOrNull)
			throws ModifyException, NavException, UnsupportedEncodingException, IOException, TranscodeException {
		if (pOwnVersionOrNull != null) {
			final VTDGen gen = new VTDGen();
			gen.enableIgnoredWhiteSpace(true);
			final XMLModifier modifier = new XMLModifier();

			if (gen.parseFile(file.getAbsolutePath(), false)) {
				final VTDNav vn = gen.getNav();
				modifier.bind(vn);

				if (vn.toElement(FC, ARTIFACT_ID)) {
					final long l = vn.expandWhiteSpaces(vn.getElementFragment(), WS_LEADING);
					final ByteArrayOutputStream out = new ByteArrayOutputStream();
					vn.dumpFragment(l, out);
					final String version = new String(out.toByteArray()).replaceAll(ARTIFACT_ID_PATTERN,
							format(VERSION_FORMAT, pOwnVersionOrNull));
					modifier.insertAfterElement(version);
				}
			}

			try (final FileOutputStream out = new FileOutputStream(file)) {
				modifier.output(out);
			}
		}
	}

	private boolean find(final Matcher updated, final Matcher original, final int originalIdx) throws IOException {
		final boolean findUpdated = updated.find();
		final boolean findOriginal = original.find(originalIdx);

		if (findOriginal && !findUpdated || !findOriginal && findUpdated) {
			throw new IOException("File cannot be updated because it has incompatbility been changed!");
		}

		return findOriginal;
	}

	@Override
	public void close() throws IOException {
		final Matcher matcher = VERSION_PATTERN.matcher(toString());
		final Matcher originalMatcher = VERSION_PATTERN.matcher(original);
		int originalIdx = 0;
		int startIdx = 0;

		while (find(matcher, originalMatcher, originalIdx)) {
			final String newVersion = matcher.group(VERSION_VALUE);
			startIdx = originalMatcher.start(VERSION_VALUE);
			original.replace(startIdx, originalMatcher.end(VERSION_VALUE), newVersion);
			originalIdx = startIdx + newVersion.length();
		}

		try (final Writer writer = new BufferedWriter(new FileWriter(file))) {
			writer.write(original.toString());
		}
	}
}
