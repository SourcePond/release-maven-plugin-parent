<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mv="http://maven.apache.org/POM/4.0.0">
	<xsl:output omit-xml-declaration="yes" indent="yes" />

	<xsl:template match="node()|@*">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*" />
		</xsl:copy>
	</xsl:template>

	<xsl:template match="/plugin/artifactId/text()">
		<xsl:value-of select="/plugin/artifactId/text()" />
		<xsl:text>-self</xsl:text>
	</xsl:template>

	<xsl:template match="/mv:project/mv:artifactId/text()">
		<xsl:value-of select="/mv:project/mv:parent/mv:artifactId/text()" />
		<xsl:text>-self</xsl:text>
		<xsl:apply-templates />
	</xsl:template>

</xsl:stylesheet>