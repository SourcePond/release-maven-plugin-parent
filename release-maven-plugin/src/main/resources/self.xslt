<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mv="http://maven.apache.org/POM/4.0.0">
	<xsl:output omit-xml-declaration="yes" indent="yes" />

	<xsl:template match="node()|@*">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*" />
		</xsl:copy>
	</xsl:template>

	<xsl:template match="/plugin/version/text()">
		<xsl:value-of select="/plugin/version/text()" />
		<xsl:text>-SELF</xsl:text>
	</xsl:template>

	<xsl:template match="/mv:project">
		<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
			xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
			<xsl:text>&#x0A;</xsl:text>
			<version>
				<xsl:value-of select="/mv:project/mv:parent/mv:version/text()" />
				<xsl:text>-SELF</xsl:text>
			</version>
			<xsl:apply-templates />
		</project>
	</xsl:template>
	
</xsl:stylesheet>