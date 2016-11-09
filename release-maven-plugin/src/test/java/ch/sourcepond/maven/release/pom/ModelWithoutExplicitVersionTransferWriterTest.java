package ch.sourcepond.maven.release.pom;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ModelWithoutExplicitVersionTransferWriterTest extends BaseVersionTransferWriterTest {

	@Override
	protected String getFileName() {
		return "original_without_explicit_version.pom";
	}

	@Override
	protected String getOwnVersionOrNull() {
		return "2.0-SNAPSHOT";
	}

	@Override
	@Test
	public void verifyStructureAfterWrite() throws Exception {
		assertEquals(format("Line %d of original is not equal to updated pom", 13),
				"	<version>2.33092323329292303382.92992839293324</version>", updatedLines.get(13));
		originalLines.add(13, updatedLines.get(13));
		super.verifyStructureAfterWrite();
	}
}
