package net.jhorstmann.jspparser;

import java.util.Collections;
import java.util.Map;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class StreamingParser extends AbstractParser {

    private ContentHandler handler;

    public StreamingParser(ContentHandler handler) {
        this.handler = handler;
    }

    public StreamingParser() {
    }

    public ContentHandler getContentHandler() {
        return handler;
    }

    public void setContentHandler(ContentHandler handler) {
        this.handler = handler;
    }

    @Override
    protected void handleStartDocument() throws SAXException {
        handler.startDocument();
        HandlerUtil.handleJspStartTag(handler, "root", Collections.EMPTY_MAP);
    }

    @Override
    protected void handleEndDocument() throws SAXException {
        HandlerUtil.handleJspEndTag(handler, "root");
        handler.endDocument();
    }

    @Override
    protected void handleDeclaration(String text) throws SAXException {
        System.out.println("decl");
        HandlerUtil.handleDeclaration(handler, text);
    }

    @Override
    protected void handleExpression(String text) throws SAXException {
        System.out.println("expr");
        HandlerUtil.handleExpression(handler, text);
    }

    @Override
    protected void handleScriptlet(String text) throws SAXException {
        System.out.println("scriptlet");
        HandlerUtil.handleScriptlet(handler, text);
    }

    @Override
    protected void handleDirective(String name, Map<String, String> attributes) throws SAXException {
        System.out.println("directive " + name);
        HandlerUtil.handleDirective(handler, name, attributes);
    }

    @Override
    protected void handleStartTag(String namespaceURI, String localName, String qualifiedName, Map<String, String> attributes) throws SAXException {
        System.out.println("start " + qualifiedName);
        HandlerUtil.handleStartTag(handler, namespaceURI, localName, qualifiedName, attributes);
    }

    @Override
    protected void handleEndTag(String namespaceURI, String localName, String qualifiedName) throws SAXException {
        System.out.println("end " + qualifiedName);
        HandlerUtil.handleEndTag(handler, namespaceURI, localName, qualifiedName);
    }

    @Override
    protected void handleTaglib(String prefix, String uri) throws SAXException {
        System.out.println("taglib " + prefix + " " + uri);
        handler.startPrefixMapping(prefix, uri);
    }

    @Override
    protected void handleText(String text) throws SAXException {
        System.out.println("text " + text);
        HandlerUtil.handleText(handler, text);
    }

    @Override
    protected void handleComment(String comment) throws SAXException {
        System.out.println("comment " + comment);
    }
}
