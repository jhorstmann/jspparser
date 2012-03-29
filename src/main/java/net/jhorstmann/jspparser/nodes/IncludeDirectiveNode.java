package net.jhorstmann.jspparser.nodes;

import java.util.Map;
import org.xml.sax.SAXException;

public class IncludeDirectiveNode extends ParentNode {

    public IncludeDirectiveNode() {
    }

    public IncludeDirectiveNode(Map<String, String> attributes) {
        super(attributes);
    }

    @Override
    public Node normalize(boolean trimWhitespace) {
        IncludeDirectiveNode norm = new IncludeDirectiveNode();
        norm.attributes = normalizeAttributes();
        norm.children = normalizeChildren(trimWhitespace);
        return norm;
    }

    @Override
    public void accept(NodeVisitor visitor) throws SAXException {
        visitor.visitIncludeDirective(this);
    }

}
