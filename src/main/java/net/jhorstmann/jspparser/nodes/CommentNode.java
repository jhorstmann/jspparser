package net.jhorstmann.jspparser.nodes;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class CommentNode extends Node {

    private String content;

    public CommentNode(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    @Override
    public void accept(NodeVisitor visitor) throws SAXException {
        visitor.visitComment(this);
    }
}
