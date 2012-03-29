package net.jhorstmann.jspparser.nodes;

import java.util.Map;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class CustomTagNode extends ParentNode {

    private String localName;
    private String namespaceURI;
    private String qualifiedName;

    public CustomTagNode(String localName, String namespaceURI, String qualifiedName) {
        this.localName = localName;
        this.namespaceURI = namespaceURI;
        this.qualifiedName = qualifiedName;
    }

    public CustomTagNode(String localName, String namespaceURI, String qualifiedName, Map<String, String> attributes) {
        super(attributes);
        this.localName = localName;
        this.namespaceURI = namespaceURI;
        this.qualifiedName = qualifiedName;
    }

    public String getLocalName() {
        return localName;
    }

    public String getNamespaceURI() {
        return namespaceURI;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    @Override
    public Node normalize(boolean trimWhitespace) {
        CustomTagNode norm = new CustomTagNode(localName, namespaceURI, qualifiedName);
        norm.attributes = normalizeAttributes();
        norm.children = normalizeChildren(trimWhitespace);
        return norm;
    }

    @Override
    public void accept(NodeVisitor visitor) throws SAXException {
        visitor.visitCustomTag(this);
    }
    
}
