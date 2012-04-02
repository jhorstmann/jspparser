package net.jhorstmann.jspparser.nodes;

import java.util.Map;
import org.xml.sax.SAXException;

public class AttributeDirectiveNode extends AttributedNode implements Directive {

    public AttributeDirectiveNode(Map<String, String> attributes) {
        super(attributes);
    }

    @Override
    public String getName() {
        return "attribute";
    }

    @Override
    public void accept(NodeVisitor visitor) throws SAXException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
