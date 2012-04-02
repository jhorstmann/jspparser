package net.jhorstmann.jspparser.nodes;

import java.util.Map;
import org.xml.sax.SAXException;

public class VariableDirectiveNode extends AttributedNode implements Directive {

    public VariableDirectiveNode(Map<String, String> attributes) {
        super(attributes);
    }

    @Override
    public String getName() {
        return "variable";
    }

    @Override
    public void accept(NodeVisitor visitor) throws SAXException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
