package net.jhorstmann.jspparser.nodes;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import static net.jhorstmann.jspparser.Constants.*;

public abstract class ScriptNode extends Node {

    protected String content;

    public ScriptNode(String content) {
        if (content == null) {
            throw new NullPointerException("Script content must not be null");
        }
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
