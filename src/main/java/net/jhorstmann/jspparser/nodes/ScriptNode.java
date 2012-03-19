package net.jhorstmann.jspparser.nodes;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import static net.jhorstmann.jspparser.Constants.*;

public abstract class ScriptNode extends Node {

    protected String content;

    public ScriptNode(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    protected void handle(ContentHandler handler, String name) throws SAXException {
        handler.startElement(NSURI_JSP, name, PREFIX_JSP + ":" + name, EMPTY_ATTRS);
        if (content != null) {
            char[] data = content.toCharArray();
            handler.characters(data, 0, data.length);
        }
        handler.endElement(NSURI_JSP, name, PREFIX_JSP + ":" + name);
    }
}
