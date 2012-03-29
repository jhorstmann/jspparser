package net.jhorstmann.jspparser.nodes;

import org.xml.sax.SAXException;

public abstract class Node {

    public Node normalize() {
        return normalize(false);
    }

    public Node normalize(boolean trimWhitespace) {
        return this;
    }

    public abstract void accept(NodeVisitor visitor) throws SAXException;
}
