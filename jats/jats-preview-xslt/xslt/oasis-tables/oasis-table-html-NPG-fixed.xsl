<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:o="http://www.niso.org/standards/z39-96/ns/oasis-exchange/table"
	xmlns:m="http://mulberrytech.com/xslt/oasis-html/util" xmlns:nls="http://namespaces.nature.com/xml/xslt/functions"
	exclude-result-prefixes="#all" version="2.0">

	<xsl:import href="oasis-table-html.xsl" />

	<xsl:template match="o:tgroup">
		<!-- NISO JATS does not include @pgwide, but OASIS permits it -->
		<xsl:variable name="pgwide" select="../@pgwide='1'" />
		<xsl:variable name="continuing" select="exists(following-sibling::o:tgroup)" />
		<xsl:variable name="continued" select="exists(following-sibling::o:tgroup)" />
		<xsl:variable name="classes"
			select="'tgroup','pgwide'[$pgwide],'cont'[$continuing],'contd'[$continued]" />
		<table>
			<xsl:call-template name="m:assign-class">
				<xsl:with-param name="class" select="string-join($classes,' ')" />
			</xsl:call-template>
			<xsl:if test="$hard-styles">
				<xsl:attribute name="style">
          <xsl:text>border-collapse: collapse</xsl:text>
          <xsl:if test="$pgwide">; width: 100%</xsl:if>
          <xsl:if test="$continuing">; margin-bottom: 0px</xsl:if>
          <xsl:if test="$continued">; marging-top: 0px</xsl:if>
        </xsl:attribute>
			</xsl:if>
			<xsl:variable name="colspecs" as="element()*" select="o:colspec" />
			<xsl:variable name="colcount" select="@cols" />
			<xsl:variable name="colwidth"
				select="xs:integer(round(100 div $colcount))" />

			<xsl:apply-templates select="* except (o:thead, o:tbody)" />
			<xsl:apply-templates select="o:thead | o:tbody">
				<xsl:with-param name="colspecs" select="$colspecs" />
				<xsl:with-param name="colcount" select="$colcount" />
				<xsl:with-param name="colwidth" select="$colwidth" />
			</xsl:apply-templates>
			<!--xsl:apply-templates/ -->
		</table>
	</xsl:template>

	<xsl:template match="o:thead">
		<xsl:param name="colspecs" as="element()*" />
		<xsl:param name="colcount" as="xs:integer*" />
		<xsl:param name="colwidth" as="xs:integer*" />
		<thead>
			<xsl:call-template name="m:assign-class" />
			<xsl:variable name="tb-border"
				select="$m:border-specs[@class='tbxx-borders']" />
			<xsl:variable name="t-border"
				select="$m:border-specs[@class='txxx-borders']" />
			<xsl:variable name="b-border"
				select="$m:border-specs[@class='xbxx-borders']" />
			<xsl:variable name="border"
				select="$m:border-specs[@class='xxxx-borders']" />
			<xsl:variable name="rows" select="o:row" />
			<xsl:variable name="nrows" select="count($rows)" />
			<xsl:for-each select="$rows">
				<xsl:variable name="next-pos" select="position() + 1" />
				<xsl:apply-templates select=".">
					<xsl:with-param name="colspecs" select="$colspecs" />
					<xsl:with-param name="border-spec"
						select="($tb-border,$t-border,$b-border,$border)" />
					<xsl:with-param name="colcount" select="$colcount" />
					<xsl:with-param name="colwidth" select="$colwidth" />
					<xsl:with-param name="rowno" select="position()" />
					<xsl:with-param name="nrows" select="$nrows" />
					<xsl:with-param name="row-type" select="'thead'" />
					<xsl:with-param name="next-row" select="$rows[$next-pos]" />
				</xsl:apply-templates>
			</xsl:for-each>
		</thead>
	</xsl:template>

	<xsl:template match="o:tbody">
		<xsl:param name="colspecs" as="element()*" />
		<xsl:param name="colcount" as="xs:integer*" />
		<xsl:param name="colwidth" as="xs:integer*" />
		<tbody>
			<xsl:call-template name="m:assign-class" />
			<xsl:variable name="border-spec"
				select="$m:border-specs[@class='xxxx-borders']" />
			<xsl:variable name="nrows" select="count(o:row)" />
			<xsl:variable name="hasFoot" select="exists(../../../table-wrap-foot)" />
			<xsl:for-each select="o:row">
				<xsl:variable name="bs"
					select="if (position() = last() and $hasFoot) then $m:border-specs[@class='xbxx-borders'] else $border-spec" />
				<xsl:apply-templates select=".">
					<xsl:with-param name="colspecs" select="$colspecs" />
					<xsl:with-param name="border-spec" select="$bs" />
					<xsl:with-param name="colcount" select="$colcount" />
					<xsl:with-param name="colwidth" select="$colwidth" />
					<xsl:with-param name="rowno" select="position()" />
					<xsl:with-param name="nrows" select="$nrows" />
					<xsl:with-param name="row-type" select="'tbody'" />
					<xsl:with-param name="next-row" select="()" />
				</xsl:apply-templates>
			</xsl:for-each>
		</tbody>
	</xsl:template>

	<xsl:template match="o:row">
		<xsl:param name="colspecs" as="element()*" />
		<xsl:param name="border-spec" as="element()*" />
		<xsl:param name="colcount" as="xs:integer*" />
		<xsl:param name="colwidth" as="xs:decimal*" />
		<xsl:param name="rowno" as="xs:integer*" />
		<xsl:param name="nrows" as="xs:integer*" />
		<xsl:param name="row-type" as="xs:string*" />
		<xsl:param name="next-row" as="item()*" />
		<!--xsl:variable name="rowno" select="m:rowno(.)"/ -->
		<xsl:variable name="here" select="." />
		<tr>
			<xsl:call-template name="m:assign-class" />
			<xsl:variable name="entries" select="count(o:entry)" />
			<xsl:for-each select="o:entry">
				<xsl:variable name="entry" select="." />
				<xsl:variable name="cnum"
					select="$colspecs[@colname=$entry/(@colname,@namest)[1]]/@colnum" />
				<xsl:variable name="colno"
					select="if ($cnum[. castable as xs:integer and number(.) gt 0]) then $cnum else position()" />
				<xsl:variable name="macross"
					select="nls:across($entry,$colno,$colspecs)" />
				<xsl:variable name="isThead" select="$row-type='thead' and $nrows > 1" />
				<xsl:if test="not($macross &lt; $colno)">
					<xsl:variable name="colspec" select="$colspecs[@colnum=$colno]" />
					<xsl:variable name="width"
						select="if ($entries=1) then '100' else if (count($macross)=1) then $colwidth else xs:string(count($macross) * $colwidth)" />
					<xsl:variable name="isEmpty"
						select="$isThead and not(normalize-space($entry))" />
					<xsl:variable name="bs"
						select="if ($isThead and $rowno=1) then $border-spec[2] else if ($isEmpty and $rowno=$nrows) then $border-spec[3] else if ($isThead and count($next-row/o:entry[$colno])=1 and not(normalize-space($next-row/o:entry[$colno])) ) then $border-spec[4] else $border-spec[1]" />
					<xsl:apply-templates
						select="nls:entry($entry,$bs,$colspec,concat($width,'%'),$macross)" />
				</xsl:if>
				<!--xsl:if test="empty(key('entry-by-row',$rowno,$here/ancestor::o:tgroup)[m:across(.)=$colno])" -->
				<xsl:if
					test="empty(key('entry-by-row',$rowno,$here/ancestor::o:tgroup)[$macross=$colno])">
					<xsl:variable name="ghost-entry">
						<o:tgroup>
							<xsl:copy-of select="$here/../parent::o:tgroup/@*" />
							<xsl:copy-of
								select="$here/../parent::o:tgroup/o:colspec[m:colno(.)=$colno][1]" />
							<xsl:for-each select="$here/..">
								<xsl:copy copy-namespaces="no">
									<xsl:copy-of select="@*" />
									<xsl:for-each select="$here">
										<xsl:copy copy-namespaces="no">
											<xsl:copy-of select="@*" />
											<o:entry>&#xA0;</o:entry>
										</xsl:copy>
									</xsl:for-each>
								</xsl:copy>
							</xsl:for-each>
						</o:tgroup>
					</xsl:variable>
					<xsl:apply-templates select="$ghost-entry//o:entry" />
				</xsl:if>
			</xsl:for-each>
		</tr>
	</xsl:template>

	<xsl:function name="nls:entry">
		<xsl:param name="object" />
		<xsl:param name="border-spec" />
		<xsl:param name="colspec" />
		<xsl:param name="width" />
		<xsl:param name="macross" as="xs:integer*" />
		<xsl:variable name="name"
			select="if (local-name($object/../..)='thead') then 'th' else 'td'" />
		<xsl:element name="{$name}">
			<xsl:if test="$hard-styles">
				<xsl:attribute name="style" select="$default-border-style" />
			</xsl:if>
			<xsl:apply-templates
				select="nls:entry-content($object,$border-spec,$colspec,$width,$macross)" />
		</xsl:element>
	</xsl:function>

	<xsl:function name="nls:entry-content">
		<xsl:param name="object" />
		<xsl:param name="border-spec" />
		<xsl:param name="colspec" />
		<xsl:param name="width" />
		<xsl:param name="macross" as="xs:integer*" />
		<!--xsl:if test="not(m:align($object)=('char'))" -->
		<xsl:variable name="align" select="nls:align($object,$colspec)" />
		<xsl:if test="not($align=('char'))">
			<xsl:attribute name="align" select="$align" />
		</xsl:if>
		<xsl:copy-of
			select="($object/@valign,$object/parent::o:row/$object/@valign,
	    $object/ancestor::*[self::o:thead|self::o:tfoot|self::o:tbody]/@valign)[1]" />
		<xsl:apply-templates select="$object/@morerows" />
		<xsl:if test="count($macross) > 1">
			<xsl:attribute name="colspan" select="count($macross)" />
		</xsl:if>

		<xsl:attribute name="class"
			select="string-join(('entry',$border-spec/@class),' ')" />
		<xsl:if test="$hard-styles">
			<xsl:attribute name="style"
				select="string-join(($default-cell-styling,$border-spec/@style),'; ')" />
		</xsl:if>

		<xsl:if test="matches($width,'[1-9]')">
			<xsl:attribute name="width" select="$width" />
		</xsl:if>
		<xsl:apply-templates select="$object" mode="cell-contents2">
			<xsl:with-param name="align" select="$align" />
			<xsl:with-param name="colspec" select="$colspec" />
		</xsl:apply-templates>

	</xsl:function>

	<xsl:template match="o:entry" mode="cell-contents2">
		<xsl:param name="align" />
		<xsl:param name="colspec" />
		<xsl:choose>
			<xsl:when test="$align = 'char'">
				<!--xsl:variable name="colspec" select="m:colspec-for-entry(.)"/ -->
				<!--xsl:variable name="char" select="(@char/string(.),$colspec/@char/string(.),'')[1]"/ -->
				<xsl:variable name="char"
					select="(@char/string(.),$colspec/@char,'')[1]" />
				<xsl:variable name="charoff"
					select="(((@charoff,$colspec/@charoff))[. castable as xs:integer]/xs:integer(.),50)[1]" />
				<span style="float:left; text-align: right; width:{$charoff}%">
					<xsl:value-of select=".[not(contains(.,$char)) or not($char)]" />
					<xsl:value-of select="substring-before(.,$char)" />
					<xsl:value-of select="$char[contains(current(),$char)]" />
				</span>
				<span style="float:left; text-align: left; width:{100 - $charoff}%">
					<xsl:value-of select="substring-after(.,$char)[$char]" />
					<xsl:value-of
						select="'&#xA0;'[not(contains(current(),$char)) or not($char)]" />
				</span>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:function name="nls:mapCol">
		<xsl:param name="colname" />
		<xsl:param name="colspecs" as="element()*" />
		<xsl:param name="colno" as="xs:integer*" />
		<xsl:variable name="num"
			select="($colspecs[@colname=$colname]/@colnum, $colno)[1]" />
		<xsl:value-of
			select="if ($num[. castable as xs:integer and number(.) gt 0]) then $num else $colno" />
	</xsl:function>

	<xsl:function name="nls:across" as="xs:integer+">
		<xsl:param name="entry" />
		<xsl:param name="colno" />
		<xsl:param name="colspecs" as="element()*" />
		<xsl:choose>
			<xsl:when test="$entry/@namest and $entry/@nameend">
				<!--xsl:sequence select="$entry/@namest to $entry/@nameend"/ -->
				<xsl:sequence
					select="nls:mapCol($entry/@namest,$colspecs,$colno) to nls:mapCol($entry/@nameend,$colspecs,$colno)" />
			</xsl:when>
			<xsl:when test="$entry/@namest">
				<xsl:sequence select="nls:mapCol($entry/@namest,$colspecs,$colno)" />
			</xsl:when>
			<xsl:when test="$entry/@nameend">
				<xsl:sequence
					select="$colno to nls:mapCol($entry/@nameend,$colspecs,$colno)" />
			</xsl:when>
			<xsl:when test="$entry/@colname">
				<xsl:sequence select="nls:mapCol($entry/@colname,$colspecs,$colno)" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:sequence select="$colno" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>

	<xsl:function name="nls:align">
		<xsl:param name="object" />
		<xsl:param name="colspec" />
		<xsl:value-of select="string(($object/@align,$colspec/@align,'left')[1])" />
	</xsl:function>

	<xsl:function name="nls:print">
		<xsl:param name="msg" as="item()*" />
		<xsl:for-each select="$msg">
			<xsl:message>
				<p>
					<xsl:value-of select="." />
				</p>
			</xsl:message>
		</xsl:for-each>
	</xsl:function>
	<xsl:function name="nls:printf">
		<xsl:param name="msg" as="item()*" />
		<xsl:message>
			<f>
				<xsl:for-each select="$msg">
					<p>
						<xsl:value-of select="." />
					</p>
				</xsl:for-each>
			</f>
		</xsl:message>
	</xsl:function>
</xsl:stylesheet>

