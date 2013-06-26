<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
      xmlns:xs="http://www.w3.org/2001/XMLSchema"
      exclude-result-prefixes="xs"
      version="2.0">
    <xsl:output method="xml" indent="yes" encoding="UTF-8"/>
    <xsl:param name="files"/>
    

    <xsl:template match="/">
        <xsl:apply-templates/>
    </xsl:template>
    
    <xsl:template match="*:schema">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
            <xsl:for-each select="tokenize($files,',')">
                <xsl:variable name="sch" select="doc(.)"/>
                <xsl:apply-templates select="$sch/*:schema/*:pattern"/>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="comment()">
        <xsl:copy-of select="."/>
    </xsl:template>
</xsl:stylesheet>
