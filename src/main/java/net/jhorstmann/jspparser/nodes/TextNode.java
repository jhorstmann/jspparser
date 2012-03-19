package net.jhorstmann.jspparser.nodes;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import static net.jhorstmann.jspparser.Constants.*;

public class TextNode extends Node {
    private String content;

    public TextNode(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void appendTo(StringBuilder appendable) {
        appendable.append(content);
    }

    @Override
    public void handle(ContentHandler handler) throws SAXException {
        if (content.length() > 0) {
            //handler.startElement(NSURI_JSP, "text", PREFIX_JSP + ":text", EMPTY_ATTRS);
            char[] chars = content.toCharArray();
            handler.characters(chars, 0, chars.length);
            //handler.endElement(NSURI_JSP, "text", PREFIX_JSP + ":text");
        }
    }

    public TextNode merge(TextNode next) {
        StringBuilder sb = new StringBuilder();
        appendTo(sb);
        next.appendTo(sb);
        return new TextNode(sb.toString());
    }

    @Override
    public String toString() {
        return getContent();
    }
}
