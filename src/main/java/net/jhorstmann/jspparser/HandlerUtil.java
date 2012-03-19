package net.jhorstmann.jspparser;

import java.util.Map;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

import static net.jhorstmann.jspparser.Constants.*;

public class HandlerUtil {

    public static Attributes convertAttributes(Map<String, String> attributes) throws SAXException {
        AttributesImpl attrs = new AttributesImpl();
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            if (value.startsWith("<%=")) {
                if (value.endsWith("%>")) {
                    value = value.substring(1, value.length() - 1);
                } else {
                    throw new SAXException("Unclosed script expression");
                }
            }
            attrs.addAttribute("", name, name, "CDATA", value);
        }
        return attrs;
    }

    public static void handleDirective(ContentHandler handler, String name, Map<String, String> attributes) throws SAXException {
        String dirname = "directive." + name;
        Attributes attrs = convertAttributes(attributes);
        handler.startElement(NSURI_JSP, dirname, PREFIX_JSP + ":" + dirname, attrs);
        handler.endElement(NSURI_JSP, dirname, PREFIX_JSP + ":" + dirname);
    }

    public static void handleDeclaration(ContentHandler handler, String text) throws SAXException {
        handler.startElement(NSURI_JSP, "declaration", PREFIX_JSP + ":declaration", EMPTY_ATTRS);
        handleCharacters(handler, text);
        handler.endElement(NSURI_JSP, "declaration", PREFIX_JSP + ":declaration");
    }

    public static void handleExpression(ContentHandler handler, String text) throws SAXException {
        handler.startElement(NSURI_JSP, "expression", PREFIX_JSP + ":expression", EMPTY_ATTRS);
        handleCharacters(handler, text);
        handler.endElement(NSURI_JSP, "expression", PREFIX_JSP + ":expression");
    }

    public static void handleScriptlet(ContentHandler handler, String text) throws SAXException {
        handler.startElement(NSURI_JSP, "scriptlet", PREFIX_JSP + ":scriptlet", EMPTY_ATTRS);
        handleCharacters(handler, text);
        handler.endElement(NSURI_JSP, "scriptlet", PREFIX_JSP + ":scriptlet");
    }

    public static void handleEndTag(ContentHandler handler, String namespaceURI, String localName, String qualifiedName) throws SAXException {
        handler.endElement(namespaceURI, localName, qualifiedName);
    }

    public static void handleStartTag(ContentHandler handler, String namespaceURI, String localName, String qualifiedName, Map<String, String> attributes) throws SAXException {
        Attributes attrs = convertAttributes(attributes);
        handler.startElement(namespaceURI, localName, qualifiedName, attrs);
    }

    public static void handleJspStartTag(ContentHandler handler, String localName, Map<String, String> attributes) throws SAXException {
        handleStartTag(handler, NSURI_JSP, localName, PREFIX_JSP + ":" + localName, attributes);
    }

    public static void handleJspEndTag(ContentHandler handler, String localName) throws SAXException {
        handleEndTag(handler, NSURI_JSP, localName, PREFIX_JSP + ":" + localName);
    }

    public static void handleComment(LexicalHandler handler, String comment) throws SAXException {
        char[] chars = comment.toCharArray();
        handler.comment(chars, 0, chars.length);
    }

    public static void handleText(ContentHandler handler, String text) throws SAXException {
        handleCharacters(handler, text);
    }

    private static void handleCharacters(ContentHandler handler, String text) throws SAXException {
        if (text.length() > 0) {
            //handler.startElement(NSURI_JSP, "text", PREFIX_JSP + ":text", EMPTY_ATTRS);
            char[] chars = text.toCharArray();
            handler.characters(chars, 0, chars.length);
            //handler.endElement(NSURI_JSP, "text", PREFIX_JSP + ":text");
        }
    }
}
