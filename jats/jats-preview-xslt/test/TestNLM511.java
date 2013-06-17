
import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;



public class TestNLM511 {

	@BeforeClass
	static public void init() {
		TestNLM485.init();
	}

	//@Test
	public void testSimple() {
		byte xmlData[] = TestNLM485.getFile("jats/jats-preview-xslt/test/xml/sdata00002a.xml");
		String xsl = "<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='2.0'> "+
				"<xsl:import href='"+ TestNLM485._xsltNPG+"'/>"+
				"<xsl:output method='html' indent='no' omit-xml-declaration='yes'/>"+
				"</xsl:stylesheet>";
		byte result[] = TestNLM485.convert(xmlData, xsl.getBytes());
		TestNLM485.writeDocument("c:/temp/t1.html", result);
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
                          "<p class=\"ref-label\"><span class=\"label\"><span class=\"generated\">1</span></span>&nbsp;<a id=\"d2e1\"><!-- named anchor --></a></p></div>"+
                          "<div class=\"ref-content cell\"><p class=\"citation\"><a id=\"d2e2\"><!-- named anchor --></a>"+
                          "K.A. Clark<i>et al.</i>Striated muscle cytoarchitecture: an intricate web of form and function.<i>Annu Rev Cell Dev Biol</i> ,2002.<b>18</b>:p. 637-706.</p></div></div>";
//		System.out.printf("i:%s\n", xml);
//		System.out.printf("e:%s\n", expected);
//		System.out.printf("a:%s\n", actual);
		assertEquals(expected, actual);
	}

}
