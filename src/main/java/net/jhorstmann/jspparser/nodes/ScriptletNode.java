package net.jhorstmann.jspparser.nodes;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class ScriptletNode extends ScriptNode {

    public ScriptletNode(String content) {
        super(content);
    }

    @Override
    public void handle(ContentHandler handler) throws SAXException {
        handle(handler, "scriptlet");
    }

}
