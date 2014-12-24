<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" 
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:c="http://www.esei.uvigo.es/dai/proyecto">

<xsl:output method="html" indent="yes" encoding="utf-8"/>
	<xsl:template match="/">
		<xsl:text disable-output-escaping="yes">&lt;!DOCTYPE HTML&gt;</xsl:text>
		<html>
			<head>
				<title>Configuration: Hybrid Server</title>
			</head>
			<body>
					<h1>Server configuration</h1>
						<h2>Connection parameters</h2>
							<xsl:apply-templates select="c:configuration/c:connections"/>
						<h2>Database parameters</h2>
							<xsl:apply-templates select="c:configuration/c:database"/>
						<h2>Servers</h2>
							<xsl:apply-templates select="c:configuration/c:servers"/>
			</body>
		</html>
	</xsl:template>
	
	<xsl:template match="c:connections">
		<div>
			<h3>http: <h4> <xsl:value-of select="c:http"/></h4></h3>
			<h3>Web Service: </h3><h4><xsl:value-of select="c:webservice"/></h4>
			<h3>Number of Clients: </h3><h4><xsl:value-of select="c:numClients"/></h4>
		</div>
	</xsl:template>
	
	<xsl:template match="c:database">
		<div>
			<h3>user: </h3><h4><xsl:value-of select="c:user"/></h4>
			<h3>password: </h3><h4><xsl:value-of select="c:password"/></h4>
			<h3>url: </h3><h4><xsl:value-of select="c:url"/></h4>
		</div>
	</xsl:template>
	
	<xsl:template match="c:servers">
		<div>
	
				<xsl:for-each select="c:server">
					<xsl:sort select="@name"/>
					<xsl:sort select="@wsdl"/>
					<xsl:sort select="@namespace"/>
					<xsl:sort select="@service"/>
					<xsl:sort select="@httpAddress"/>
					<xsl:value-of select="."/>
				</xsl:for-each>
		</div>
	</xsl:template>
	
	
</xsl:stylesheet>