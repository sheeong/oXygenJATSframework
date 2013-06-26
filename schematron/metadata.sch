<?xml version="1.0" encoding="UTF-8"?>
<!--

* Schematron rules for testing semantic validity of XML files in the JATS DTD submitted to NPG *

Due to the configuration of XSLT templates used in the validation service, attributes cannot be used as the 'context' of a rule.

For example, context="article[@article-type]" will recognise the context as 'article' with an 'article-type' attribute, but context="article/@article-type" will set context as 'article'.
Use the <let> element to define the attribute if necessary.

-->
<schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">
  <title>Schematron rules for NPG content in JATS v1.0</title>
  <let name="allowed-values" value="document( 'allowed-values-nlm.xml' )/allowed-values"/><!--Points at document containing information on journal titles, ids and DOIs-->
  <let name="products" value="document('products.owl')"/>
  <ns uri="http://ns.nature.com/terms/" prefix="terms"/><!--Namespace for Ontologies document-->
  <ns uri="http://purl.org/dc/elements/1.1/" prefix="dc"/><!--Namespace for Ontologies document-->
  <ns uri="http://www.w3.org/1999/02/22-rdf-syntax-ns#" prefix="rdf"/><!--Namespace for Ontologies document-->
  
  <!--Regularly used values throughout rules-->
  <let name="journal-title" value="//journal-meta/journal-title-group/journal-title"/>
  <let name="journal-id" value="//journal-meta/journal-id"/>
    
  <!--
    ******************************************************************************************************************************
    Root
    ******************************************************************************************************************************
  -->
  
  <!--article/@article-type exists and matches expected values for journal-->
  <pattern>
    <rule context="article" role="error"><!--Does the article have an article-type attribute-->
      <assert  id="article1" test="@article-type">All articles should have an article-type attribute on "article". The value should be the same as the information contained in the subject element with attribute content-type="article-type".</assert>
    </rule>
  </pattern>
  
  <!--pattern>
    <rule context="article[@article-type]" role="error">Is the article-type valid?
      <assert  id="article2" test="$journal-title = $allowed-values/article-types/article-type[@type=$article-type]/journal or not($journal-title) or not($products[descendant::dc:title=$journal-title])">Unexpected root article type (<value-of select="$article-type"/>) for <value-of select="$journal-title"/>.</assert>
    </rule>
  </pattern-->
 
  <pattern>
    <rule context="article[@xml:lang]" role="error"><!--If @xml:lang exists, does it have an allowed value-->
      <let name="lang" value="@xml:lang"></let>
      <assert  id="article3" test="$allowed-values/languages/language[.=$lang]">Unexpected language (<value-of select="$lang"/>) declared on root article element. Expected values are "en" (English), "de" (German) and "ja" (Japanese/Kanji).</assert>
    </rule>
  </pattern>
  
  <!--no processing instructions in the file-->
  
  <!--
    ******************************************************************************************************************************
    Front 
    ******************************************************************************************************************************
  -->
  
  <!-- ======================================================== Journal metadata =============================================== -->
  
  <pattern>
    <rule context="journal-id" role="error"><!--Correct attribute value included-->
      <assert id="jmeta1" test="@journal-id-type='publisher'">The "journal-id" element should have attribute: journal-id-type="publisher".</assert>
    </rule>
  </pattern>
  
  <pattern>
    <rule context="journal-meta" role="error"><!--Journal title exists-->
      <assert id="jmeta2a" test="descendant::journal-title-group/journal-title">Journal title is missing from the journal metadata section. Other rules are based on having a correct journal title and therefore will not be run. Please resubmit this file when the title has been added.</assert>
    </rule>
  </pattern>
  <pattern>
    <rule context="journal-title-group" role="error"><!--only one journal-title-group-->
      <report id="jmeta2b" test="preceding-sibling::journal-title-group">Only one journal-title-group should be used.</report>
    </rule>
  </pattern>
  
  <pattern>
    <rule context="journal-title-group" role="error"><!--Is the journal title valid-->
      <assert id="jmeta3a" test="not(descendant::journal-title) or $products[descendant::dc:title=$journal-title]">Journal titles must be from the prescribed list of journal names. "<value-of select="$journal-title"/>" is not on this list - check spelling, spacing of words or use of the ampersand. Other rules are based on having a correct journal title and therefore will not be run. Please resubmit this file when the title has been corrected.</assert>
      </rule>
    </pattern>
  <pattern>
    <rule context="journal-title-group" role="error"><!--Is the journal id valid?-->
      <assert id="jmeta3b" test="$products[descendant::terms:pcode=$journal-id] or not($products[descendant::dc:title=$journal-title])">Journal id is incorrect. For <value-of select="$journal-title"/>, it should be: <value-of select="$products//*[child::dc:title=$journal-title]/terms:pcode"/>. Other rules are based on having a correct journal id and therefore will not be run. Please resubmit this file when the journal id has been corrected.</assert></rule>
    </pattern>
  <pattern>
    <rule context="journal-title-group" role="error"><!--Do the journal title and id match each other?-->
      <assert id="jmeta3c" test="$journal-id=$products//*[child::dc:title=$journal-title]/terms:pcode or not($products[descendant::dc:title=$journal-title]) or not($products[descendant::terms:pcode=$journal-id])">Journal id (<value-of select="$journal-id"/>) does not match journal title: <value-of select="$journal-title"/>. Check which is the correct value.</assert>
    </rule>
  </pattern>
  
  <pattern>
    <rule context="journal-subtitle | trans-title-group" role="error"><!--No other children of journal-title-group used-->
      <report id="jmeta4" test="parent::journal-title-group">Unexpected use of "<name/>" in "journal-title-group".</report>
    </rule>
  </pattern>
  
  <pattern>
    <rule context="journal-title-group/journal-title" role="error"><!--Only one journal title present-->
      <report id="jmeta4b" test="preceding-sibling::journal-title">More than one journal title found. Only one journal title should be used.</report>
    </rule>
  </pattern>
  
  <pattern>
    <rule context="journal-title-group/abbrev-journal-title" role="error"><!--Only one journal title present-->
      <report id="jmeta4c" test="preceding-sibling::abbrev-journal-title">More than one abbreviated journal title found. Only one abbreviated journal title should be used.</report>
    </rule>
  </pattern>
  
  <pattern>
    <rule context="journal-meta/issn" role="error"><!--Correct attribute value inserted; ISSN matches expected syntax-->
      <assert id="jmeta5a" test="@pub-type='ppub' or @pub-type='epub'">ISSN should have attribute pub-type="ppub" for print or pub-type="epub" for electronic publication.</assert>
      </rule>
    </pattern>
  <pattern>
    <rule context="journal-meta/issn" role="error">
      <let name="issn" value="concat('http://ns.nature.com/publications/',.)"/>
      <assert id="jmeta5b" test="not($journal-title) or not($products[descendant::dc:title=$journal-title]) or $products//*[child::dc:title=$journal-title][terms:hasPublication[@rdf:resource=$issn]]">Unexpected ISSN value for <value-of select="$journal-title"/> (<value-of select="."/>)</assert>
    </rule>
  </pattern>
  
  <pattern>
    <rule context="journal-meta/isbn" role="error"><!--Other expected and unexpected elements-->
      <report id="jmeta6" test=".">Do not use the ISBN element in journal metadata.</report>
    </rule>
  </pattern>
  
  <pattern>
    <rule context="journal-meta" role="error"><!--Other expected and unexpected elements-->
      <assert id="jmeta7a" test="publisher">Journal metadata should include a "publisher" element.</assert>
    </rule>
  </pattern>
  
  <pattern>
    <rule context="publisher" role="error">
      <report id="jmeta7b" test="publisher-loc">Do not use "publisher-loc" element in publisher information.</report>
    </rule>
  </pattern>
  
  <pattern>
    <rule context="journal-title-group | journal-title | publisher">
      <report id="jmeta8a" test="@content-type">Unnecessary use of "content-type" attribute on "<name/>" element.</report>
    </rule>
  </pattern>
 
</schema>
