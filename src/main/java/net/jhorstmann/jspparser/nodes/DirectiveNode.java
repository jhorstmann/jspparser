package net.jhorstmann.jspparser.nodes;

import java.util.Map;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import static net.jhorstmann.jspparser.Constants.*;

public class DirectiveNode extends AttributedNode {

    private String name;

    public DirectiveNode(String name) {
        this.name = name;
    }

    public DirectiveNode(String name, Map<String, String> attributes) {
        super(attributes);
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    @Override
    public void handle(ContentHandler handler) throws SAXException {
        if (!"include".equals(name) && !"taglib".equals(name)) {
            Attributes attrs = convertAttributes(true);
            handler.startElement(NSURI_JSP, "directive." + name, PREFIX_JSP + ":directive." + name, attrs);
            handler.endElement(NSURI_JSP, "directive." + name, PREFIX_JSP + ":directive." + name);
        }
    }

}
