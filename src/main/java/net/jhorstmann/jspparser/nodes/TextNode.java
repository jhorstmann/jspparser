package net.jhorstmann.jspparser.nodes;

import org.xml.sax.SAXException;

public class TextNode extends Node {
    private String content;

    public TextNode(String content) {
        if (content == null) {
            throw new NullPointerException("Text content must not be null");
        }
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void appendTo(StringBuilder appendable) {
        appendable.append(content);
    }

    public TextNode merge(TextNode next) {
        StringBuilder sb = new StringBuilder();
        appendTo(sb);
        next.appendTo(sb);
        return new TextNode(sb.toString());
    }

    @Override
    public String toString() {
        return getContent();
    }

    @Override
    public void accept(NodeVisitor visitor) throws SAXException {
        visitor.visitText(this);
    }

}
