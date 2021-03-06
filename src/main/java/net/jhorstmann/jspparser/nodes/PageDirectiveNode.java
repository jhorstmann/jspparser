package net.jhorstmann.jspparser.nodes;

import java.util.Map;
import org.xml.sax.SAXException;

public class PageDirectiveNode extends AttributedNode implements Directive {

    public PageDirectiveNode(Map<String, String> attributes) {
        super(attributes);
    }

    @Override
    public String getName() {
        return "page";
    }

    @Override
    public void accept(NodeVisitor visitor) throws SAXException {
        visitor.visitPageDirective(this);
    }
}
