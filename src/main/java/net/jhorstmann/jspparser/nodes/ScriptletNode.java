package net.jhorstmann.jspparser.nodes;

import org.xml.sax.SAXException;

public class ScriptletNode extends ScriptNode {

    public ScriptletNode(String content) {
        super(content);
    }

    @Override
    public void accept(NodeVisitor visitor) throws SAXException {
        visitor.visitScriptlet(this);
    }

}
