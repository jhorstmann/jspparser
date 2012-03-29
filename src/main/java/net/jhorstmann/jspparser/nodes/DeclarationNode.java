package net.jhorstmann.jspparser.nodes;

import org.xml.sax.SAXException;

public class DeclarationNode extends ScriptNode {

    public DeclarationNode(String content) {
        super(content);
    }

    @Override
    public void accept(NodeVisitor visitor) throws SAXException {
        visitor.visitDeclaration(this);
    }

}
