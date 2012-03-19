package net.jhorstmann.jspparser;

import net.jhorstmann.jspparser.nodes.CustomTagNode;
import net.jhorstmann.jspparser.nodes.Node;
import net.jhorstmann.jspparser.nodes.TextNode;
import static org.junit.Assert.*;
import org.junit.Test;

public class NodeTest {

    @Test
    public void testMergeOnlyText() {
        CustomTagNode elem = new CustomTagNode("elem", "http://example.com/", "ns:elem");
        elem.addChildNode(new TextNode("Hello"));
        elem.addChildNode(new TextNode(" "));
        elem.addChildNode(new TextNode("World"));
        assertNotNull(elem.getChildNodes());
        assertEquals(3, elem.getChildNodes().size());
        CustomTagNode normalized = (CustomTagNode) elem.normalize();
        assertEquals("elem", normalized.getLocalName());
        assertEquals("http://example.com/", normalized.getNamespaceURI());
        assertNotNull(normalized.getChildNodes());
        assertEquals(1, normalized.getChildNodes().size());
        Node text = normalized.getChildNodes().get(0);
        assertTrue(text instanceof TextNode);
        assertEquals("Hello World", ((TextNode) text).getContent());

    }

    @Test
    public void testMergeTextAndElements() {
        CustomTagNode elem = new CustomTagNode("elem", "http://example.com/", "ns:elem");
        elem.addChildNode(new CustomTagNode("child", "http://example.com/", "ns:child"));
        elem.addChildNode(new TextNode("Hello"));
        elem.addChildNode(new TextNode(" "));
        elem.addChildNode(new TextNode("World"));
        elem.addChildNode(new CustomTagNode("child", "http://example.com/", "ns:child"));
        elem.addChildNode(new TextNode("This"));
        elem.addChildNode(new TextNode(" is a "));
        elem.addChildNode(new TextNode("Test"));
        elem.addChildNode(new CustomTagNode("child", "http://example.com/", "ns:child"));
        CustomTagNode normalized = (CustomTagNode) elem.normalize();
        assertNotNull(normalized.getChildNodes());
        System.out.println(normalized.getChildNodes());
        assertEquals(5, normalized.getChildNodes().size());
        Node text1 = normalized.getChildNodes().get(1);
        assertTrue(text1 instanceof TextNode);
        assertEquals("Hello World", ((TextNode) text1).getContent());
        Node text2 = normalized.getChildNodes().get(3);
        assertTrue(text2 instanceof TextNode);
        assertEquals("This is a Test", ((TextNode) text2).getContent());

    }

    @Test
    public void testMergeWhitespace() {
        CustomTagNode elem = new CustomTagNode("elem", "http://example.com/", "ns:elem");
        elem.addChildNode(new TextNode("  \t  \r\n  "));
        elem.addChildNode(new TextNode("  \r\n  "));
        assertNotNull(elem.getChildNodes());
        assertEquals(2, elem.getChildNodes().size());
        CustomTagNode normalized = (CustomTagNode) elem.normalize(true);
        assertEquals("elem", normalized.getLocalName());
        assertEquals("http://example.com/", normalized.getNamespaceURI());
        assertNotNull(normalized.getChildNodes());
        assertEquals(0, normalized.getChildNodes().size());
    }
}
