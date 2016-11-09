package ch.sourcepond.maven.release.pom;

import static ch.sourcepond.maven.release.pom.VersionTransferWriter.VERSION_PATTERN;
import static ch.sourcepond.maven.release.pom.VersionTransferWriter.VERSION_VALUE;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.apache.commons.io.FileUtils.copyInputStreamToFile;
import static org.apache.commons.io.FileUtils.readLines;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.DefaultModelReader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.junit.Before;
import org.junit.Test;

public abstract class BaseVersionTransferWriterTest {
	protected static final String[] EXPECTED_VERSIONS = new String[] { "1.1938382293932932.3002923834838284",
			"2.33092323329292303382.92992839293324", "3.3", "4", "5.23423", "6.32923493929239392.329392932939293",
			"7" };
	private final File originalPom = new File(format("target/original_%s.pom", randomUUID()));
	private final MavenXpp3Writer mavenXpp3Writer = new MavenXpp3Writer();
	protected List<String> originalLines;
	protected List<String> updatedLines;
	protected Model updatedModel;
	private VersionTransferWriter writer;

	protected abstract String getFileName();

	protected abstract String getOwnVersionOrNull();

	@Before
	public void setup() throws Exception {
		try (final InputStream in = getClass().getResourceAsStream("/version-transfer/" + getFileName())) {
			copyInputStreamToFile(in, originalPom);
		}

		originalPom.deleteOnExit();
		originalLines = readLines(originalPom);

		final DefaultModelReader reader = new DefaultModelReader();
		updatedModel = reader.read(originalPom, Collections.<String, Object> emptyMap());

		updatedModel.getParent().setVersion(EXPECTED_VERSIONS[0]);
		updatedModel.setVersion(EXPECTED_VERSIONS[1]);
		updatedModel.getDependencyManagement().getDependencies().get(0).setVersion(EXPECTED_VERSIONS[2]);
		updatedModel.getDependencyManagement().getDependencies().get(1).setVersion(EXPECTED_VERSIONS[3]);
		updatedModel.getDependencies().get(0).setVersion(EXPECTED_VERSIONS[4]);
		updatedModel.getDependencies().get(1).setVersion(EXPECTED_VERSIONS[5]);
		updatedModel.getDependencies().get(2).setVersion(EXPECTED_VERSIONS[6]);

		writer = new VersionTransferWriter(originalPom, getOwnVersionOrNull());
		try {
			mavenXpp3Writer.write(writer, updatedModel);
		} finally {
			writer.close();
		}

		updatedLines = readLines(originalPom);
	}

	protected void verifyUpdatedVersion(final String originalLine, final String updatedLine,
			final String expectedVersion) {
		assertEquals(VERSION_PATTERN.matcher(originalLine).replaceAll(""),
				VERSION_PATTERN.matcher(updatedLine).replaceAll(""));
		final Matcher matcher = VERSION_PATTERN.matcher(updatedLine);
		matcher.find();
		assertEquals(expectedVersion, matcher.group(VERSION_VALUE));
	}

	@Test
	public void verifyStructureAfterWrite() throws Exception {
		assertEquals(49, originalLines.size());
		assertEquals(originalLines.size(), updatedLines.size());

		for (int i = 0, verIdx = 0; i < originalLines.size(); i++) {
			if (i == 6 || i == 13 || i == 21 || i == 26 || i == 35 || i == 40 || i == 45) {
				verifyUpdatedVersion(originalLines.get(i), updatedLines.get(i), EXPECTED_VERSIONS[verIdx++]);
				continue;
			}
			assertEquals(format("Line %d of original is not equal to updated pom", i), originalLines.get(i),
					updatedLines.get(i));
		}
	}

}
