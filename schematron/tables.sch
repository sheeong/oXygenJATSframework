<?xml version="1.0" encoding="UTF-8"?>
<!--
    
* Schematron rules for NPG JATS XML *

Due to the configuration of XSLT templates used in the validation service, attributes cannot be used as the 'context' of a rule.

For example, context="article[@article-type]" will recognise the context as 'article' with an 'article-type' attribute, but context="article/@article-type" will set context as 'article'.
Use the <let> element to define the attribute if necessary.

-->
<schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">
    
<pattern>
    <rule context="table-wrap-foot/fn" role="error">
        <let name="id" value="@id"/>
        <assert id="tab1a" test="ancestor::article//xref[@ref-type='table-fn'][@rid=$id]">Table footnote is not linked to. Either insert a correctly numbered link, or just mark up as a table footer paragraph.</assert>
    </rule>
</pattern>

    <pattern>
        <rule context="table-wrap-foot/fn" role="error">
            <let name="id" value="@id"/>
            <assert id="tab1b" test="not(ancestor::article//xref[@ref-type='table-fn'][@rid=$id]) or label">Table footnote should contain "label" element - check if it is a footnote or should just be a table footer paragraph.</assert>
        </rule>
    </pattern>

    <pattern>
        <rule context="xref[@ref-type='table-fn']" role="error"><!--Does symbol in link match symbol on footnote?-->
            <let name="id" value="@rid"/>
            <let name="sup-link" value="descendant::text()"/>
            <let name="sup-fn" value="ancestor::article//table-wrap-foot/fn[@id=$id]/label//text()"/>
            <assert id="tab1c" test="not($sup-fn) or not($sup-link) or $sup-link=$sup-fn">Mismatch on linking text: "<value-of select="$sup-link"/>" in table, but "<value-of select="$sup-fn"/>" in footnote. Please check that correct footnote has been linked to.</assert>
        </rule>
    </pattern>


</schema>