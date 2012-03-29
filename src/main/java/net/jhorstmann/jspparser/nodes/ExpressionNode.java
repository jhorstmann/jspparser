package net.jhorstmann.jspparser.nodes;

import org.xml.sax.SAXException;

public class ExpressionNode extends ScriptNode {

    public ExpressionNode(String content) {
        super(content);
    }

    @Override
    public void accept(NodeVisitor visitor) throws SAXException {
        visitor.visitExpression(this);
    }

}
