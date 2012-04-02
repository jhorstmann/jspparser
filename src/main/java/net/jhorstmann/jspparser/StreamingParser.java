package net.jhorstmann.jspparser;

import java.util.Collections;
import java.util.Map;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import static net.jhorstmann.jspparser.Constants.*;
import net.jhorstmann.jspparser.nodes.AttributedNode;
import org.xml.sax.Attributes;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

public class StreamingParser extends AbstractParser implements XMLReader {

    private ContentHandler handler;
    private EntityResolver resolver;
    private LexicalHandler lexicalHandler;

    public StreamingParser(ContentHandler handler) {
        this.handler = handler;
    }

    public StreamingParser() {
    }

    @Override
    public ContentHandler getContentHandler() {
        return handler;
    }

    @Override
    public void setContentHandler(ContentHandler handler) {
        this.handler = handler;
    }

    public LexicalHandler getLexicalHandler() {
        return lexicalHandler;
    }

    public void setLexicalHandler(LexicalHandler lexicalHandler) {
        this.lexicalHandler = lexicalHandler;
    }

    @Override
    public void setEntityResolver(EntityResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public EntityResolver getEntityResolver() {
        return resolver;
    }

    @Override
    public void setDTDHandler(DTDHandler handler) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DTDHandler getDTDHandler() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setErrorHandler(ErrorHandler handler) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ErrorHandler getErrorHandler() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        if ("http://xml.org/sax/features/namespaces".equals(name)) {
            return true;
        } else if ("http://xml.org/sax/features/namespace-prefixes".equals(name)) {
            return false;
        } else {
            throw new SAXNotRecognizedException("Feature " + name);
        }
    }

    @Override
    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
        if ("http://xml.org/sax/features/namespaces".equals(name)) {
            if (!value) {
                throw new SAXNotSupportedException();
            }
        } else if ("http://xml.org/sax/features/namespace-prefixes".equals(name)) {
            if (value) {
                throw new SAXNotSupportedException();
            }
        } else {
            throw new SAXNotRecognizedException("Feature " + name);
        }
    }

    @Override
    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        if ("http://xml.org/sax/properties/lexical-handler".equals(name)) {
            return lexicalHandler;
        } else {
            throw new SAXNotRecognizedException("Property " + name);
        }
    }

    @Override
    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        if ("http://xml.org/sax/properties/lexical-handler".equals(name)) {
            setLexicalHandler((LexicalHandler) value);
        } else {
            throw new SAXNotRecognizedException("Property " + name);
        }
    }

    private void handleCharacters(String text) throws SAXException {
        if (text.length() > 0) {
            char[] chars = text.toCharArray();
            handler.characters(chars, 0, chars.length);
        }
    }

    @Override
    protected void handleStartDocument() throws SAXException {
        handler.startDocument();
        handler.startElement(NSURI_JSP, "root", PREFIX_JSP + ":root", EMPTY_ATTRS);
    }

    @Override
    protected void handleEndDocument() throws SAXException {
        handler.endElement(NSURI_JSP, "root", PREFIX_JSP + ":root");
        handler.endDocument();
    }

    @Override
    protected void handleDeclaration(String text) throws SAXException {
        handler.startElement(NSURI_JSP, "declaration", PREFIX_JSP + ":declaration", EMPTY_ATTRS);
        handleCharacters(text);
        handler.endElement(NSURI_JSP, "declaration", PREFIX_JSP + ":declaration");
    }

    @Override
    protected void handleExpression(String text) throws SAXException {
        handler.startElement(NSURI_JSP, "expression", PREFIX_JSP + ":expression", EMPTY_ATTRS);
        handleCharacters(text);
        handler.endElement(NSURI_JSP, "expression", PREFIX_JSP + ":expression");
    }

    @Override
    protected void handleScriptlet(String text) throws SAXException {
        handler.startElement(NSURI_JSP, "scriptlet", PREFIX_JSP + ":scriptlet", EMPTY_ATTRS);
        handleCharacters(text);
        handler.endElement(NSURI_JSP, "scriptlet", PREFIX_JSP + ":scriptlet");
    }

    @Override
    protected void handleAttributeDirective(Map<String, String> attributes) throws SAXException {
    }

    @Override
    protected void handleTagDirective(Map<String, String> attributes) throws SAXException {
    }

    @Override
    protected void handleVariableDirective(Map<String, String> attributes) throws SAXException {
    }

    @Override
    protected void handleStartIncludeDirective(Map<String, String> attributes) throws SAXException {
    }

    @Override
    protected void handleEndIncludeDirective() throws SAXException {
    }

    @Override
    protected void handlePageDirective(Map<String, String> attributes) throws SAXException {
    }

    @Override
    protected void handleTaglibDirective(Map<String, String> attributes) throws SAXException {
    }

    @Override
    protected void handleStartTag(String namespaceURI, String localName, String qualifiedName, Map<String, String> attributes) throws SAXException {
        Attributes attrs = AttributedNode.convertAttributes(attributes, true);
        handler.startElement(namespaceURI, localName, qualifiedName, attrs);
    }

    @Override
    protected void handleEndTag(String namespaceURI, String localName, String qualifiedName) throws SAXException {
        handler.endElement(namespaceURI, localName, qualifiedName);
    }

    @Override
    protected void handleTaglib(String prefix, String uri) throws SAXException {
        handler.startPrefixMapping(prefix, uri);
    }

    @Override
    protected void handleText(String text) throws SAXException {
        //handler.startElement(NSURI_JSP, "text", PREFIX_JSP + ":text", EMPTY_ATTRS);
        handleCharacters(text);
        //handler.endElement(NSURI_JSP, "text", PREFIX_JSP + ":text");
    }

    @Override
    protected void handleComment(String comment) throws SAXException {
        if (lexicalHandler != null) {
            char[] chars = comment.toCharArray();
            lexicalHandler.comment(chars, 0, chars.length);
        }

    }
}
