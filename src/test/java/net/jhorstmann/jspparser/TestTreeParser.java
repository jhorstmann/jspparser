package net.jhorstmann.jspparser;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.Assert;
import net.jhorstmann.jspparser.nodes.CustomTagNode;
import net.jhorstmann.jspparser.nodes.DirectiveNode;
import net.jhorstmann.jspparser.nodes.Node;
import net.jhorstmann.jspparser.nodes.RootNode;
import net.jhorstmann.jspparser.nodes.TextNode;
import org.junit.Test;
import org.xml.sax.SAXException;

public class TestTreeParser {

    private DirectiveNode parseDirective(String content) throws IOException, SAXException {
        TreeParser p = new TreeParser();
        p.parse(new StringReader(content));
        return (DirectiveNode) p.getFirstChild();
    }

    @Test
    public void testParseDirective() throws IOException, SAXException {
        DirectiveNode directive = parseDirective("<%@page pageEncoding='utf-8' %>");
        Assert.assertNotNull(directive);
        Assert.assertEquals("page", directive.getName());
        Map<String, String> attrs = directive.getAttributes();
        Assert.assertNotNull(attrs);
        Assert.assertEquals(1, attrs.size());
        Assert.assertEquals(Collections.singletonMap("pageEncoding", "utf-8"), attrs);
    }

    @Test
    public void testParseDirectiveLeadingSpace() throws IOException, SAXException {
        DirectiveNode directive = parseDirective("<%@ page pageEncoding='utf-8'%>");
        Assert.assertNotNull(directive);
        Assert.assertEquals("page", directive.getName());
        Map<String, String> attrs = directive.getAttributes();
        Assert.assertNotNull(attrs);
        Assert.assertEquals(1, attrs.size());
        Assert.assertEquals(Collections.singletonMap("pageEncoding", "utf-8"), attrs);
    }

    @Test
    public void testParseDirectiveMultipleAttrs() throws IOException, SAXException {
        DirectiveNode directive = parseDirective("<%@page pageEncoding='utf-8' info=\"Test\" %>");
        Assert.assertNotNull(directive);
        Assert.assertEquals("page", directive.getName());
        Map<String, String> attrs = directive.getAttributes();
        Assert.assertNotNull(attrs);
        Assert.assertEquals(2, attrs.size());
        HashMap<String, String> expected = new HashMap<String, String>();
        expected.put("pageEncoding", "utf-8");
        expected.put("info", "Test");
        Assert.assertEquals(expected, attrs);
    }

    @Test
    public void testImportDirective() throws IOException, SAXException {
        DirectiveNode directive = parseDirective("<%@page import='a.A;' %>");
        Assert.assertNotNull(directive);
        Assert.assertEquals("page", directive.getName());
        Map<String, String> attrs = directive.getAttributes();
        Assert.assertNotNull(attrs);
        Assert.assertEquals(1, attrs.size());
        Assert.assertEquals(Collections.singletonMap("import", "a.A;"), attrs);
    }

    @Test
    public void testImportDirectiveMulti() throws IOException, SAXException {
        DirectiveNode directive = parseDirective("<%@page import='a.A' import=\"b.B\" %>");
        Assert.assertNotNull(directive);
        Assert.assertEquals("page", directive.getName());
        Map<String, String> attrs = directive.getAttributes();
        Assert.assertNotNull(attrs);
        Assert.assertEquals(1, attrs.size());
        Assert.assertEquals(Collections.singletonMap("import", "a.A,b.B"), attrs);
    }

    @Test
    public void testDirectives() throws IOException, SAXException {
        String content = "<%@page encoding='utf-8' %>\r\n<%@ taglib prefix='test' tagdir='/WEB-INF/tags' %>";
        TreeParser p = new TreeParser();
        p.parse(new StringReader(content));
        RootNode root = p.getRootNode();
        Assert.assertNotNull(root);
        List<Node> children = root.getChildNodes();
        Assert.assertNotNull(children);
        Assert.assertEquals(3, children.size());
        DirectiveNode page = (DirectiveNode) children.get(0);
        TextNode text = (TextNode) children.get(1);
        Assert.assertEquals("\r\n", text.getContent());
        DirectiveNode taglib = (DirectiveNode) children.get(2);
        Map<String, String> attrs = taglib.getAttributes();
        Assert.assertEquals(2, attrs.size());
    }

    @Test
    public void testEmptyCustomTag() throws IOException, SAXException {
        String content = "<t:test/>";
        TreeParser p = new TreeParser();
        p.addTaglib("t", "/WEB-INF/tags");
        p.parse(new StringReader(content));
        RootNode root = p.getRootNode();
        List<Node> children = root.getChildNodes();
        Assert.assertNotNull(children);
        System.out.println(children);
        Assert.assertEquals(1, children.size());
        CustomTagNode tag = (CustomTagNode) children.get(0);
        Assert.assertEquals("t:test", tag.getQualifiedName());
        Assert.assertEquals("test", tag.getLocalName());
        Assert.assertEquals("/WEB-INF/tags", tag.getNamespaceURI());
    }
    @Test
    public void testCustomTag() throws IOException, SAXException {
        String content = "<t:test>test</t:test>";
        TreeParser p = new TreeParser();
        p.addTaglib("t", "/WEB-INF/tags");
        p.parse(new StringReader(content));
        RootNode root = p.getRootNode();
        List<Node> children = root.getChildNodes();
        Assert.assertNotNull(children);
        System.out.println(children);
        Assert.assertEquals(1, children.size());
        CustomTagNode tag = (CustomTagNode) children.get(0);
        Assert.assertEquals("t:test", tag.getQualifiedName());
        Assert.assertEquals("test", tag.getLocalName());
        Assert.assertEquals("/WEB-INF/tags", tag.getNamespaceURI());
        children = tag.getChildNodes();
        Assert.assertEquals(1, children.size());
        TextNode text = (TextNode) children.get(0);
        Assert.assertEquals("test", text.getContent());
    }
}
