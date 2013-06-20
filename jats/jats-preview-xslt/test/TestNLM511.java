
import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;



public class TestNLM511 {

	@BeforeClass
	static public void init() {
		TestNLM485.init();
	}

	//@Test
	public void testSimple() {
		//byte xmlData[] = TestNLM485.getFile("jats/jats-preview-xslt/test/xml/sdata00002a.xml");
		byte xmlData[] = TestNLM485.getFile("jats/jats-preview-xslt/test/xml/nutd201121a.xml");
		String xsl = "<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='2.0'> "+
				"<xsl:import href='"+ TestNLM485._citationXslt+"'/>"+
				"<xsl:output method='xml' indent='no' omit-xml-declaration='yes'/>"+
				"</xsl:stylesheet>";
		String xsl2 = "<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='2.0'> "+
				"<xsl:import href='"+ TestNLM485._xsltTable+"'/>"+
				"<xsl:output method='xml' indent='no' omit-xml-declaration='yes'/>"+
				"</xsl:stylesheet>";
		String xsl3 = "<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='2.0'> "+
				"<xsl:import href='"+ TestNLM485._xsltNPG+"'/>"+
				"<xsl:output method='html' indent='no' omit-xml-declaration='yes'/>"+
				"</xsl:stylesheet>";

		boolean testSource = new String(xmlData).indexOf("<source>") != -1;
		int count = 0;

		byte result[] = TestNLM485.convert(xmlData, xsl.getBytes()); 
		TestNLM485.writeDocument("c:/temp/t1.xml", result);
		if (testSource) {
			Assert.assertTrue(new String(result).indexOf("<source>") != -1);
			++count;
		}
		result = TestNLM485.convert(result, xsl2.getBytes());
		if (testSource) {
			Assert.assertTrue(new String(result).indexOf("<source>") != -1);
			++count;
		}
		TestNLM485.writeDocument("c:/temp/t2.xml", result);
		result = TestNLM485.convert(result, xsl3.getBytes());
		if (testSource) {
			Assert.assertTrue(new String(result).indexOf("<source>") == -1);
			++count;
		}
		TestNLM485.writeDocument("c:/temp/t1.html", result);
		if (testSource) {
			assertEquals(3, count);
		}
	}

	@Test
	public void testCitation() {
		String xml = "<mixed-citation publication-type=\"journal\"><name><surname>Clark</surname><given-names>K.A.</given-names></name><etal/><article-title>Striated muscle cytoarchitecture: an intricate web of form and function</article-title>.<source>Annu Rev Cell Dev Biol</source> ,<year>2002</year>.<volume>18</volume>:p. <fpage>637</fpage>-<lpage>706</lpage>.</mixed-citation>";
		String xsl = "<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='2.0'> "+
				"<xsl:import href='"+ TestNLM485._citationXslt+"'/>"+
				"<xsl:output method='xml' indent='no' omit-xml-declaration='yes'/>"+
				"<xsl:strip-space elements='*'/>"+
				"</xsl:stylesheet>";
		byte result[] = TestNLM485.convert(xml.getBytes(), xsl.getBytes());
		String actual = new String(result);
		String expected = "\n<!DOCTYPE mixed-citation\n  PUBLIC \"-//NLM//DTD Journal Publishing DTD v3.0 20080202//EN\" \"journalpublishing3.dtd\">\n"+
				"<mixed-citation publication-type=\"journal\">Clark K.A.<etal/>Striated muscle cytoarchitecture: an intricate web of form and function.<source>Annu Rev Cell Dev Biol</source> ,2002.<volume>18</volume>:p. 637-706.</mixed-citation>";
		//		System.out.printf("i:%s\n", xml);
		//		System.out.printf("e:%s\n", expected);
		//		System.out.printf("a:%s\n", actual);
		assertEquals(expected, actual);

	}

	@Test
	public void testFragment() {
		String xml = "<ref><mixed-citation publication-type=\"journal\"><name><surname>Clark</surname><given-names>K.A.</given-names></name><etal/><article-title>Striated muscle cytoarchitecture: an intricate web of form and function</article-title>.<source>Annu Rev Cell Dev Biol</source> ,<year>2002</year>.<volume>18</volume>:p. <fpage>637</fpage>-<lpage>706</lpage>.</mixed-citation></ref>";
		String xsl = "<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='2.0'> "+
				"<xsl:import href='"+ TestNLM485._xsltNPG+"'/>"+
				"<xsl:output method='html' indent='no' omit-xml-declaration='yes'/>"+
				"<xsl:strip-space elements='*'/>"+
				"<xsl:template match='/'>" +
				"<xsl:apply-templates select='ref'/>"+
				"</xsl:template>"+
				"</xsl:stylesheet>";
		byte result[] = TestNLM485.convert(xml.getBytes(), xsl.getBytes());
		String actual = new String(result);
		//System.out.println(new String(actual));
		String expected = "<!DOCTYPE html\n  PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n"+
				"<div class=\"row\">" +
				"<div class=\"ref-label cell\">"+
				"<p class=\"ref-label\"><span class=\"label\"><span class=\"generated\">1</span></span>&nbsp;</p></div>"+
				"<div class=\"ref-content cell\"><p class=\"citation\">"+
				"K.A. Clark<i>et al.</i>Striated muscle cytoarchitecture: an intricate web of form and function.<i>Annu Rev Cell Dev Biol</i> ,2002.<b>18</b>:p. 637-706.</p></div></div>";
		//		System.out.printf("i:%s\n", xml);
		//		System.out.printf("e:%s\n", expected);
		//		System.out.printf("a:%s\n", actual);
		int ix = actual.indexOf("<a");
		int iy = actual.indexOf("</a>");
		if (ix != -1 && iy != -1) {
			actual = actual.substring(0, ix).concat(actual.substring(iy+4));

			ix = actual.indexOf("<a");
			iy = actual.indexOf("</a>");
			if (ix != -1 && iy != -1) actual = actual.substring(0, ix).concat(actual.substring(iy+4));
		}
		assertEquals(expected, actual);
	}

}
