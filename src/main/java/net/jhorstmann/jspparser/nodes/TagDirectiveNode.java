package net.jhorstmann.jspparser.nodes;

import java.util.Map;
import org.xml.sax.SAXException;

public class TagDirectiveNode extends AttributedNode implements Directive {

    public TagDirectiveNode(Map<String, String> attributes) {
        super(attributes);
    }

    @Override
    public String getName() {
        return "tag";
    }

    @Override
    public void accept(NodeVisitor visitor) throws SAXException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
