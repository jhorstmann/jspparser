package net.jhorstmann.jspparser.nodes;

import java.util.Map;
import org.xml.sax.SAXException;

public class TaglibDirectiveNode extends AttributedNode {

    public TaglibDirectiveNode(Map<String, String> attributes) {
        super(attributes);
    }

    @Override
    public void accept(NodeVisitor visitor) throws SAXException {
        visitor.visitTaglibDirective(this);
    }

}
