import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * unit test for JATS to HTML conversion including oasis table transformation
 * 1st transformation - citations formatting - citations-prep/jats-PMCcit.xsl
 * 2nd transformation - oasis table          - oasis-tables/oasis-table-html-NPG-fixed.xsl
 * 3rd transformation - jats to html         - main/jats-html-NPG-fixed.xsl
 * 
 * fixed:
 * 1. affiliation references
 * 2. alignment of char column
 * 3. poor performance of oasis table transformation
 *
 * Assumption:
 * require test xml files in directory jats/jats-preview-xslt/test/xml
 * 
 * @author shee.ong
 *
 */
public class TestNLM485 {

	final static private String _topDir= "jats/jats-preview-xslt/xslt/";
	final static public String _citationXslt =_topDir.concat("citations-prep/jats-PMCcit.xsl");
	final static public String _xsltTable = _topDir.concat("oasis-tables/oasis-table-html-NPG-fixed.xsl");
	//final static private String _xsltOTable = _topDir.concat("oasis-tables/oasis-table-html.xsl");
	final static public String _xsltNPG  = _topDir.concat("main/jats-html-NPG-fixed.xsl");

	final static private File _xslts[] = {new File(_citationXslt), new File(_xsltTable), new File(_xsltNPG)};

	private static Processor _proc;
	private static XsltCompiler _comp;
	private static DocumentBuilder _builder;
	private static SAXParser _parser;
	
	@BeforeClass
	static public void init() {
		_proc = new Processor(false);
		_comp = _proc.newXsltCompiler();
		_builder = _proc.newDocumentBuilder();
		_builder.setDTDValidation(false);
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		saxParserFactory.setNamespaceAware(true);
		saxParserFactory.setValidating(false);
		try {
			saxParserFactory.setFeature("http://xml.org/sax/features/validation", false);
		    saxParserFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
		    saxParserFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		    saxParserFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
		    saxParserFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			_parser = saxParserFactory.newSAXParser();
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//@Test
	public void testSimple() {
		byte xmlData[] = getFile("jats/jats-preview-xslt/test/xml/nutd201228a.xml");
		testTable(xmlData, _xsltTable, null);
	}
	
	@Test
	public void testTransform() {
		File dir = new File("jats/jats-preview-xslt/test/xml");
		String dest = "jats/jats-preview-xslt/test/html/";
		long now = System.currentTimeMillis();
		int count=0;
		for (File file : dir.listFiles()) {
			byte jats[] = getFile(file.getPath());
			byte result[] = convert(jats, _xslts);
			writeDocument(dest.concat(file.getName().replace(".xml",".html")), result);
			++count;
		}
		System.out.printf("Transformed %d files in %dms\n", count, System.currentTimeMillis() - now);
	}

	
	
	/**
	 * 
	 * @param xmlData
	 * @param xslt
	 * @param sb
	 */
	public static void testTable(byte xmlData[], String xslt, StringBuffer sb) {
		String citXsl = "<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='2.0'>"+
				"<xsl:import href='"+_citationXslt+"'/>"+
				"<xsl:output method='xml' indent='no'  doctype-public='-//NLM//DTD JATS (Z39.96) Journal Publishing DTD with OASIS Tables v1.0 20120330//EN' doctype-system='JATS-journalpublishing-oasis-article1.dtd'/>"+
				"</xsl:stylesheet>";
		String xsl = "<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='2.0'>"+
				"<xsl:import href='"+xslt+"'/>"+
				"<xsl:output method='xml' indent='yes'/>"+
				"</xsl:stylesheet>";
		String xsl2 = "<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='2.0'>"+
				"<xsl:import href='"+_xsltNPG+"'/>"+
				"</xsl:stylesheet>";
		writeDocument("c:/temp/jats.xml", xmlData);
		byte citations[] = convert(xmlData, citXsl.getBytes());
		//TestUtils.writeDocument("c:/temp/cit.xml", citations);

		long now = System.currentTimeMillis();
		byte result[] = convert(citations, xsl.getBytes());
		long now2 = System.currentTimeMillis();
		System.out.printf("first transformation took: %d\n", now2 - now);
		//if (sb != null) sb.append(new String(result));
		writeDocument("c:/temp/t1.xml", result);
		
		byte html[] = convert(result, xsl2.getBytes());
		System.out.printf("second transformation took: %d\n", System.currentTimeMillis() - now2);
		if (sb != null) sb.append(new String(html));
		writeDocument("c:/temp/t2.html", html);
		//System.out.println(new String(html));
	}

	
	@Test
	/**
	 * test oasis table performance
	 */
	public void testTableSize() {
		testTableSize(100, 40); //test 100 rows, 40 columns table
	}


	/**
	 * 
	 * @param rows
	 * @param columns
	 */
	private void testTableSize(int rows, int columns) {
		String xsl = "<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='2.0'>"+
				"<xsl:import href='"+_xsltTable+"'/>"+
				"<xsl:output method='xml' indent='yes'/>"+
				"</xsl:stylesheet>";
		String xsl2 = "<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='2.0'>"+
				"<xsl:import href='"+_xsltNPG+"'/>"+
				"</xsl:stylesheet>";
		long now = System.currentTimeMillis();
		byte result[] = convert(generateTable(columns,rows), xsl.getBytes());
		long now2=System.currentTimeMillis();
		System.out.printf("1st: %d\n", now2 - now);
		byte html[] = convert(result, xsl2.getBytes());
		System.out.printf("2nd: %d\n", System.currentTimeMillis() - now2);
	}


	/**
	 * generate test data for different table size - rows and columns
	 * @param columns
	 * @param rows
	 * @return
	 */
	private byte[] generateTable(int columns, int rows) {
		StringBuffer sb = new StringBuffer();
		sb.append("<xsl:stylesheet xmlns:o='http://www.niso.org/standards/z39-96/ns/oasis-exchange/table' xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='2.0'>");
		sb.append("<o:table frame='topbot' colsep='0' rowsep='0'>");
		sb.append("<o:tgroup cols='");
		sb.append(""+columns);
		sb.append("'>");
		for (int i=1; i <= columns; i++) {
			sb.append("<o:colspec colnum='"+ i + "' colname='" + i +"' align='char' char='(' colwidth='1*'/>");
		}

		sb.append("<o:thead>");
		sb.append("<o:row rowsep='1'>");
		for (int i=1; i <= columns; i++) {
			sb.append("<o:entry align='center'>" + i + "</o:entry>");
		}
		sb.append("</o:row></o:thead>");
		sb.append("<o:tbody>");
		int count=1;
		for (int r=1; r <= rows; r++) {
			sb.append("<o:row>");
			for (int i=0; i < columns; i++) {
				sb.append("<o:entry>" + count++ + "</o:entry>");
			}
			sb.append("</o:row>");
		}
		sb.append("</o:tbody></o:tgroup></o:table>");
		sb.append("</xsl:stylesheet>");

		return sb.toString().getBytes();
	}

	@Test
	/**
	 * test column name mapping
	 */
	public void testColname() {
		String xml = "<oasis:table xmlns:oasis='http://www.niso.org/standards/z39-96/ns/oasis-exchange/table' frame=\'topbot\' colsep=\'0\' rowsep=\'0\'>"+
                "<oasis:tgroup cols='5'>"+
                    "<oasis:colspec colnum='1' colname='c1' align='left' colwidth='1*'/>"+
                    "<oasis:colspec colnum='2' colname='c2' align='center' colwidth='1*'/>"+
                    "<oasis:colspec colnum='3' colname='c3' align='center' colwidth='1*'/>"+
                    "<oasis:colspec colnum='4' colname='c4' align='char' char='&#xB1;' colwidth='1*'/>"+
                    "<oasis:colspec colnum='5' colname='c5' align='center' colwidth='1*'/>"+
                    "<oasis:thead>"+
                    "    <oasis:row>"+
                    "        <oasis:entry>(A)</oasis:entry>"+
                    "        <oasis:entry align='center' namest='c2' nameend='c3'/>"+
                    "        <oasis:entry align='center' namest='c4' nameend='c5'/>"+
                    "    </oasis:row>"+
                    "    <oasis:row>"+
                    "        <oasis:entry align='center' colname='c1' />"+
                    "        <oasis:entry align='center' rowsep='1' namest='c2' nameend='c3'><italic>Obese; BMI</italic> &#x2A7E;<italic>25 (</italic>N<italic>=262)</italic></oasis:entry>"+
                    "        <oasis:entry align='center' rowsep='1' namest='c4' nameend='c5'><italic>Non-obese; BMI &lt;25 (</italic>N<italic>=136)</italic></oasis:entry>"+
                    "    </oasis:row>"+
                    "    <oasis:row rowsep='1'>"+
                    "        <oasis:entry align='center' namest='cc1' />"+
                    "        <oasis:entry align='center'><italic>Mean&#xB1;s.d.</italic></oasis:entry>"+
                    "        <oasis:entry align='center'><italic>Min&#x2013;Max</italic></oasis:entry>"+
                    "        <oasis:entry align='center'><italic>Mean&#xB1;s.d.</italic></oasis:entry>"+
                    "        <oasis:entry align='center'><italic>Min&#x2013;Max</italic></oasis:entry>"+
                    "    </oasis:row>"+
                    "</oasis:thead>"+
                    "<oasis:tbody>"+
                "    <oasis:row>"+
                "    <oasis:entry>Age (years)</oasis:entry>"+
                "    <oasis:entry align='char' char='&#xB1;'>35&#xB1;10.45</oasis:entry>"+
                "    <oasis:entry>18&#x2013;72</oasis:entry>"+
                "    <oasis:entry>31&#xB1;8.51</oasis:entry>"+
                "    <oasis:entry>18&#x2013;72</oasis:entry>"+
                "</oasis:row>"+
                "<oasis:row>"+
                "    <oasis:entry>BW (kg)</oasis:entry>"+
                "    <oasis:entry align='char' char='&#xB1;'>93.0&#xB1;22.77</oasis:entry>"+
                "    <oasis:entry>57.6&#x2013;188.5</oasis:entry>"+
                "    <oasis:entry>55.8&#xB1;8.05</oasis:entry>"+
                "    <oasis:entry>41.0&#x2013;77.9</oasis:entry>"+
                "</oasis:row>"+
                "</oasis:tbody>"+
                "</oasis:tgroup>"+
                "</oasis:table>";
		String xsl = "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"2.0\" "+
                "xmlns:nls=\"http://namespaces.nature.com/xml/xslt/functions\" "+
                     " xmlns:o='http://www.niso.org/standards/z39-96/ns/oasis-exchange/table'>"+
				"<xsl:import href=\""+_xsltTable+"\"/>"+
                "<xsl:output method='text' omit-xml-declaration='yes'/>"+
				"<xsl:template match=\"o:table\">"+
				"<xsl:variable name=\"colspecs\" select=\"o:tgroup/o:colspec\"/>"+
				"<xsl:sequence select=\"nls:across(o:tgroup/o:thead/o:row[2]/o:entry[3],4,$colspecs)\"/>"+ //expects 4 5
				"<xsl:sequence select=\"nls:across(o:tgroup/o:thead/o:row[1]/o:entry[2],2,$colspecs)\"/>"+ //expects 2 3
				"<xsl:sequence select=\"nls:across(o:tgroup/o:thead/o:row[2]/o:entry[1],1,$colspecs)\"/>"+ //expects 1
				"<xsl:sequence select=\"nls:across(o:tgroup/o:thead/o:row[3]/o:entry[1],1,$colspecs)\"/>"+ //expects 1
				"<xsl:sequence select=\"nls:across(o:tgroup/o:tbody/o:row[2]/o:entry[4],4,$colspecs)\"/>"+ //expects 4
				"</xsl:template>"+
				"</xsl:stylesheet>";
		byte out[] = convert(xml.getBytes(), xsl.getBytes());
		String actual = new String(out);
		//System.out.println(actual);
		assertEquals("4 5 2 3 1 1 4", actual);
	}

	@Test
	public void testColnameMapping() {
		String xml = "<oasis:table xmlns:oasis='http://www.niso.org/standards/z39-96/ns/oasis-exchange/table' frame=\'topbot\' colsep=\'0\' rowsep=\'0\'>"+
                    "<oasis:colspec colnum='2' colname='colN' align='char' char='&#xB1;' colwidth='1*'/>"+
                    "<oasis:entry colname='colN'><italic>Mean&#xB1;s.d.</italic></oasis:entry>"+
                    "<oasis:entry><italic>Mean&#xB1;s.d.</italic></oasis:entry>"+
                    "<oasis:entry><italic>Mean&#xB1;s.d.</italic></oasis:entry>"+
                    "</oasis:table>";
		String xsl = "<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='2.0' "+
                "xmlns:nls='http://namespaces.nature.com/xml/xslt/functions' "+
                     " xmlns:o='http://www.niso.org/standards/z39-96/ns/oasis-exchange/table'>"+
				"<xsl:import href=\""+_xsltTable+"\"/>"+
                "<xsl:output method='text' omit-xml-declaration='yes'/>"+
				"<xsl:template match='o:table'>"+
				"<xsl:variable name='colspecs' select='o:colspec'/>"+
				"<xsl:sequence select='nls:across(o:entry[1],1,$colspecs)'/>"+ //expects 2
				"<xsl:sequence select='nls:across(o:entry[3],3,$colspecs)'/>"+ //expects 3
				"</xsl:template>"+
				"</xsl:stylesheet>";
		byte out[] = convert(xml.getBytes(), xsl.getBytes());
		String actual = new String(out);
		//System.out.println(actual);
		assertEquals("2 3", actual);
	}

	@Test
	public void testCharColumn() {
		String xml = "<oasis:table xmlns:oasis='http://www.niso.org/standards/z39-96/ns/oasis-exchange/table' frame=\'topbot\' colsep=\'0\' rowsep=\'0\'>"+
                "<oasis:tgroup cols='5'>"+
                    "<oasis:colspec colnum='1' colname='c1' align='left' colwidth='1*'/>"+
                    "<oasis:colspec colnum='2' colname='c2' align='center' colwidth='1*'/>"+
                    "<oasis:colspec colnum='3' colname='c3' align='center' colwidth='1*'/>"+
                    "<oasis:colspec colnum='4' colname='c4' align='char' char='&#xB1;' colwidth='1*'/>"+
                    "<oasis:colspec colnum='5' colname='c5' align='center' colwidth='1*'/>"+
                    "<oasis:thead>"+
                    "    <oasis:row>"+
                    "        <oasis:entry>(A)</oasis:entry>"+
                    "        <oasis:entry align='center' namest='c2' nameend='c3'/>"+
                    "        <oasis:entry align='center' namest='c4' nameend='c5'/>"+
                    "    </oasis:row>"+
                    "    <oasis:row>"+
                    "        <oasis:entry align='center'/>"+
                    "        <oasis:entry align='center' rowsep='1' namest='c2' nameend='c3'><italic>Obese; BMI</italic> &#x2A7E;<italic>25 (</italic>N<italic>=262)</italic></oasis:entry>"+
                    "        <oasis:entry align='center' rowsep='1' namest='c4' nameend='c5'><italic>Non-obese; BMI &lt;25 (</italic>N<italic>=136)</italic></oasis:entry>"+
                    "    </oasis:row>"+
                    "    <oasis:row rowsep='1'>"+
                    "        <oasis:entry align='center'/>"+
                    "        <oasis:entry align='center'><italic>Mean&#xB1;s.d.</italic></oasis:entry>"+
                    "        <oasis:entry align='center'><italic>Min&#x2013;Max</italic></oasis:entry>"+
                    "        <oasis:entry align='center'><italic>Mean&#xB1;s.d.</italic></oasis:entry>"+
                    "        <oasis:entry align='center'><italic>Min&#x2013;Max</italic></oasis:entry>"+
                    "    </oasis:row>"+
                    "</oasis:thead>"+
                    "<oasis:tbody>"+
                "    <oasis:row>"+
                "    <oasis:entry>Age (years)</oasis:entry>"+
                "    <oasis:entry align='char' char='&#xB1;'>35&#xB1;10.45</oasis:entry>"+
                "    <oasis:entry>18&#x2013;72</oasis:entry>"+
                "    <oasis:entry>31&#xB1;8.51</oasis:entry>"+
                "    <oasis:entry>18&#x2013;72</oasis:entry>"+
                "</oasis:row>"+
                "<oasis:row>"+
                "    <oasis:entry>BW (kg)</oasis:entry>"+
                "    <oasis:entry align='char' char='&#xB1;'>93.0&#xB1;22.77</oasis:entry>"+
                "    <oasis:entry align='char' char='&#x2013;'>57.6&#x2013;188.5</oasis:entry>"+
                "    <oasis:entry>55.8&#xB1;8.05</oasis:entry>"+
                "    <oasis:entry>41.0&#x2013;77.9</oasis:entry>"+
                "</oasis:row>"+
                "</oasis:tbody>"+
                "</oasis:tgroup>"+
                "</oasis:table>";
		String xsl = "<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='2.0' "+
				"xmlns:o='http://www.niso.org/standards/z39-96/ns/oasis-exchange/table' >" +
				"<xsl:import href='"+_xsltTable+"'/>"+
				"<xsl:output method='xml' indent='no'/>"+
				"<xsl:template match='o:table'>"+
				"<xsl:apply-templates/>"+
				"</xsl:template>"+
				"</xsl:stylesheet>";
		String xsl2 = "<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='2.0' "+
                "xmlns:nls='http://namespaces.nature.com/xml/xslt/functions' >"+
				"<xsl:import href='"+_xsltNPG+"'/>"+
				"<xsl:template match='table'>"+
				"<xsl:apply-templates select='thead/tr[2]'/>"+
				"<xsl:apply-templates select='tbody/tr[2]/td[4]'/>"+
				"<xsl:apply-templates select='tbody/tr[2]/td[3]'/>"+
				"</xsl:template>"+
				"</xsl:stylesheet>";
		byte result[] = convert(xml.getBytes(), xsl.getBytes());
		byte html[] = convert(result, xsl2.getBytes());
		String actual = new String(html);
		//System.out.println(actual);
		String expected="<span style=\"float:left; text-align: right; width:50%\">55.8±</span><span style=\"float:left; text-align: left; width:50%\">8.05</span></td>";
		assertTrue(actual.indexOf(expected) != -1);
		expected="<span style=\"float:left; text-align: right; width:50%\">57.6–</span><span style=\"float:left; text-align: left; width:50%\">188.5</span>";
		assertTrue(actual.indexOf(expected) != -1);
		expected="<i>Obese; BMI</i> ⩾<i>25 (</i>N<i>=262)</i>";
		assertTrue(actual.indexOf(expected) != -1);
	}

	@Test
	public void testCharAlignment() {
		String xml = 
                "<oasis:row xmlns:oasis='http://www.niso.org/standards/z39-96/ns/oasis-exchange/table'>"+
                "    <oasis:entry align='char' char='&#xB1;'>35&#xB1;10.45</oasis:entry>"+
                "</oasis:row>";
		String xsl = "<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='2.0' "+
				"xmlns:nls='http://namespaces.nature.com/xml/xslt/functions' "+
				"xmlns:o='http://www.niso.org/standards/z39-96/ns/oasis-exchange/table'  exclude-result-prefixes='o nls'>" +
				"<xsl:import href='"+_xsltTable+"'/>"+
				"<xsl:output method='xml' indent='no' omit-xml-declaration='yes'/>"+
				"<xsl:template match='o:row'>"+
				"<tr><td><xsl:apply-templates select=\"nls:entry-content(o:entry,(),(),'10',1)\"/></td></tr>"+
				"</xsl:template>"+
				"</xsl:stylesheet>";
		String xsl2 = "<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='2.0' "+
                "xmlns:nls='http://namespaces.nature.com/xml/xslt/functions' exclude-result-prefixes='nls' >"+
				"<xsl:import href='"+_xsltNPG+"'/>"+
				"<xsl:output method='xml' indent='no' omit-xml-declaration='yes'/>"+
				"<xsl:template match='tr'>"+
				"<xsl:apply-templates select='td'/>"+
				"</xsl:template>"+
				"</xsl:stylesheet>";
		byte result[] = convert(xml.getBytes(), xsl.getBytes());
		String actual = new String(result);
		//System.out.println(new String(actual));
		String expected = "<tr><td class=\"entry\" style=\"border-color: black; border-width: thin; padding: 5px\" width=\"10\"><span style=\"float:left; text-align: right; width:50%\">35±</span><span style=\"float:left; text-align: left; width:50%\">10.45</span></td></tr>";
		assertEquals(expected, actual);
		byte html[] = convert(result, xsl2.getBytes());
		actual = new String(html);
		//System.out.println(actual);
        expected = "<span style=\"float:left; text-align: right; width:50%\">35±</span><span style=\"float:left; text-align: left; width:50%\">10.45</span></td></body></html>";
		assertTrue(actual.endsWith(expected));
	}


	@Test
	public void testAffiliation() {
		String xml = "<test>"+
	                 "<xref ref-type='aff' rid='a1'/>"+
				     "<xref ref-type='corresp' rid='c1'/>"+
	                 "<aff id='a1'>affiliation</aff>"+
	                 "<author-notes><corresp id='c1'>correspondent</corresp></author-notes>"+
				     "</test>";
		String xsl = "<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='2.0'>"+
				"<xsl:import href='"+_xsltNPG+"'/>"+
				"<xsl:output method='html' indent='no'/>"+
				"<xsl:template match='test'>"+
				"<xsl:apply-templates select='xref'/>"+
				"<xsl:apply-templates mode='metadata' select='aff'/>"+
				"<xsl:apply-templates mode='metadata' select='author-notes/corresp'/>"+
				"</xsl:template>"+
				"</xsl:stylesheet>";
		byte out[] = convert(xml.getBytes(), xsl.getBytes());
		String actual = new String(out);
		String expected = "<a href=\"#a1\"><span class=\"generated\"><span>a1</span></span></a><a href=\"#c1\"><span class=\"generated\"><span>c1</span></span></a><p class=\"metadata-entry\"><a id=\"a1\"><span class=\"generated\">[</span>a1<span class=\"generated\">]</span><!-- named anchor --></a>affiliation</p><p class=\"metadata-entry\"><span class=\"generated\"><a id=\"c1\"><!-- named anchor --></a>Correspondence: </span><span class=\"generated\">[</span>c1<span class=\"generated\">] </span>correspondent</p></body></html>";
		//System.out.println(actual);
		assertTrue(actual.endsWith(expected));
	}

	
	public static byte[] getFile(String filename) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			DataInputStream dis = new DataInputStream(new FileInputStream(filename));
			byte data[] = new byte[1024];
			int len;
			while ( (len=dis.read(data)) != -1 ) {
				baos.write(data, 0, len);
			}
			dis.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return baos.toByteArray();
	}

	static public void writeDocument(String filename, String content) {
		try {
			BufferedWriter bfw = new BufferedWriter(new FileWriter(filename));
			bfw.write(content);
			bfw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * overloaded writeDocument
	 * @param filename
	 * @param content
	 */
	static public void writeDocument(String filename, byte content[]) {
		writeDocument(filename, new String(content));
	}

	public static byte[] convert(byte xml[], byte xslt[]) {
		try {
			XsltExecutable template = _comp.compile(new StreamSource(new ByteArrayInputStream(xslt)));
			return convert(xml, template);
		} catch (SaxonApiException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e.getMessage());
		}
	}
	
	private static byte[] convert(byte xmlData[], XsltExecutable template) {
		if (xmlData == null) return null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Exception ec = null;
		try {
			XsltTransformer transformer = template.load();
			//transformer.setErrorListener(_errorListener);
			Source xml = new SAXSource(_parser.getXMLReader(), new InputSource(new ByteArrayInputStream(xmlData)));
			
			Serializer out = new Serializer();
			out.setOutputStream(baos);

			transformer.setInitialContextNode(_builder.build(xml));
			transformer.setDestination(out);
			transformer.transform();
			
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			ec=e;
		} catch (SaxonApiException e) {
			// TODO Auto-generated catch block
			ec=e;
		}
		if (ec != null) throw new RuntimeException(ec.getMessage());
		return baos.toByteArray();
	}

	static public byte[] convert(byte[] xmlData, File... xslts) {
		if (xslts == null || xslts.length==0) return null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			XsltExecutable template =  _comp.compile(new StreamSource(xslts[0]));
			//if (xslts.length==1) return convert(xmlData, new StreamSource(xslts[0]));
			XsltTransformer trans1 = template.load();
			Serializer out = new Serializer();
			out.setOutputStream(baos);
			Source xml = new SAXSource(_parser.getXMLReader(), new InputSource(new ByteArrayInputStream(xmlData)));
			trans1.setInitialContextNode(_builder.build(xml));
			XsltTransformer trans = trans1;
			for (int i=1; i < xslts.length; i++) {
				XsltExecutable ex2 = _comp.compile(new StreamSource(xslts[i]));// getTemplate(new StreamSource(xslts[i]));
				XsltTransformer trans2 = ex2.load();
				trans.setDestination(trans2);
				trans = trans2;
			}
			trans.setDestination(out);
			trans1.transform();
		} catch (SaxonApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return baos.toByteArray();
	}
}
