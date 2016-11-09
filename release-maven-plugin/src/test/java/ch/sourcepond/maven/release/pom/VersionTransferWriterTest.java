package ch.sourcepond.maven.release.pom;

/**
 *
 */
public class VersionTransferWriterTest extends BaseVersionTransferWriterTest {

	@Override
	protected String getFileName() {
		return "original.pom";
	}

	@Override
	protected String getOwnVersionOrNull() {
		return null;
	}

}
