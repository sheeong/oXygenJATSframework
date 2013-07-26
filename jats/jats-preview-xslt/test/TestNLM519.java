import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


public class TestNLM519 {
	@BeforeClass
	static public void init() {
		TestNLM485.init();
	}

	@Test
	public void testSimple() {
		byte xmlData[] = TestNLM485.getFile("jats/jats-preview-xslt/test/xml/table_formatting.xml");
		String xsl = "<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='2.0'> "+
				"<xsl:import href='"+ TestNLM485._citationXslt+"'/>"+
				"<xsl:output method='xml' indent='yes' omit-xml-declaration='yes'/>"+
				"</xsl:stylesheet>";
		String xsl2 = "<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='2.0'> "+
				"<xsl:import href='"+ TestNLM485._xsltTable+"'/>"+
				"<xsl:output method='xml' indent='yes' omit-xml-declaration='yes'/>"+
				"</xsl:stylesheet>";
		String xsl3 = "<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='2.0'> "+
				"<xsl:import href='"+ TestNLM485._xsltNPG+"'/>"+
				"<xsl:output method='html' indent='yes' omit-xml-declaration='yes'/>"+
				"</xsl:stylesheet>";

		byte result[] = TestNLM485.convert(xmlData, xsl.getBytes()); 
		TestNLM485.writeDocument("c:/temp/t1.xml", result);
		result = TestNLM485.convert(result, xsl2.getBytes());
		TestNLM485.writeDocument("c:/temp/t2.xml", result);
		result = TestNLM485.convert(result, xsl3.getBytes());
		TestNLM485.writeDocument("c:/temp/t1.html", result);
	}

	@Test
	public void testFootnote() {
		String xml = "<table-wrap-foot><fn id=\"t1-fn1\"><label><sup>a</sup></label><p>Data on participant characteristics obtained at the time of the adiposity measurements are presented. Median (range) for continuous traits and number of subjects (percent) for categorical traits are compared by ethnicity, with <italic>P</italic>-values from non-parametric Wilcoxon rank sum test and the <italic>&#x3C7;</italic><sup>2</sup>-test of association, respectively.</p></fn>"+
                     //"<fn id=\"t1-fn2\"><label><sup>b</sup></label><p>Data from the baseline questionnaire (1993&#x2013;1996) of the Multiethnic Cohort Study. Current or recent smokers were excluded from the study.</p></fn>"+
				     "</table-wrap-foot>";
		String xsl = "<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='2.0'> "+
				"<xsl:import href='"+ TestNLM485._xsltNPG+"'/>"+
				"<xsl:output method='html' indent='no' omit-xml-declaration='yes'/>"+
				"<xsl:strip-space elements='*'/>"+
				"<xsl:template match='/'>"+
				"<xsl:apply-templates select='table-wrap-foot/fn'/>"+
				"</xsl:template>"+
				"</xsl:stylesheet>";
		byte result[] = TestNLM485.convert(xml.getBytes(), xsl.getBytes());
		String actual = new String(result);
		String expected = "<!DOCTYPE html\n  PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n"+
				"<div class=\"footnote\"><a id=\"t1-fn1\"><!-- named anchor --></a><p id=\"d153e6\"><sup>a</sup> Data on participant characteristics obtained at the time of the adiposity measurements are presented. Median (range) for continuous traits and number of subjects (percent) for categorical traits are compared by ethnicity, with <i>P</i>-values from non-parametric Wilcoxon rank sum test and the <i>χ</i><sup>2</sup>-test of association, respectively.</p></div>";
				//"<div class=\"footnote\"><a id=\"t1-fn2\"><!-- named anchor --></a><p id=\"d2e20\"><sup>b</sup> Data from the baseline questionnaire (1993–1996) of the Multiethnic Cohort Study. Current or recent smokers were excluded from the study.</p>"
		//		System.out.printf("i:%s\n", xml);
		//		System.out.printf("e:%s\n", expected);
		//		System.out.printf("a:%s\n", actual);
		assertEquals(expected, actual);

	}

	@Test
	public void testCharCol() {

		String xml = "<oasis:table xmlns:oasis='http://www.niso.org/standards/z39-96/ns/oasis-exchange/table' frame=\'topbot\' colsep=\'0\' rowsep=\'0\'>"+
                "<oasis:tgroup cols='2'>"+
                    "<oasis:colspec colnum='1' colname='c1' align='left' colwidth='1*'/>"+
                    "<oasis:colspec colnum='2' colname='c2' align='char' char='.' colwidth='1*'/>"+
                    "<oasis:thead>"+
                    "    <oasis:row rowsep='1'>"+
                    "        <oasis:entry namest='c1'>thead c1</oasis:entry>"+
                    "        <oasis:entry><bold>Bold</bold> c2</oasis:entry>"+
                    "    </oasis:row>"+
                    "</oasis:thead>"+
                    "<oasis:tbody>"+
                "    <oasis:row>"+
                "    <oasis:entry><italic>Row 1</italic> c1</oasis:entry>"+
                "    <oasis:entry><italic>Row 1</italic> c2</oasis:entry>"+
                "</oasis:row>"+
                "</oasis:tbody>"+
                "</oasis:tgroup>"+
                "</oasis:table>";
		String xsl = "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"2.0\" "+
                     " xmlns:oasis='http://www.niso.org/standards/z39-96/ns/oasis-exchange/table'>"+
     				"<xsl:import href=\""+TestNLM485._xsltTable+"\"/>"+
                    "<xsl:output method='xml' indent='yes' omit-xml-declaration='yes'/>"+
     				"<xsl:template match='/'>"+
                    "<xsl:apply-templates select='oasis:table'/>"+
                    "</xsl:template>"+
     				"</xsl:stylesheet>";
		byte out[] = TestNLM485.convert(xml.getBytes(), xsl.getBytes());
		String actual = new String(out);
		//System.out.println(actual);
		Assert.assertFalse(actual.indexOf("Row 1 c2") != -1);
		Assert.assertTrue(actual.indexOf("Row 1</italic> c2</td>") != -1);
		Assert.assertTrue(actual.indexOf("Row 1</italic> c1</td>") != -1);

	}
}
