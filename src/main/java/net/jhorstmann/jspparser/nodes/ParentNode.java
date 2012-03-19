package net.jhorstmann.jspparser.nodes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public abstract class ParentNode extends AttributedNode {

    protected List<Node> children;

    public ParentNode() {
    }

    public ParentNode(Map<String, String> attributes) {
        super(attributes);
    }

    public void addChildNode(Node node) {
        getChildNodes().add(node);
    }

    public List<Node> getChildNodes() {
        if (children == null) {
            children = new ArrayList<Node>();
        }
        return children;
    }

    protected void handleChildren(ContentHandler handler) throws SAXException {
        if (children != null) {
            for (Node node : children) {
                node.handle(handler);
            }
        }
    }

    protected List<Node> normalizeChildren(boolean trimWhitespace) {
        if (children == null || children.isEmpty()) {
            return null;
        } else {
            List<Node> normalizedChildren = new ArrayList<Node>(children.size());
            for (Iterator<Node> i = children.iterator(); i.hasNext();) {
                Node node = i.next().normalize();
                if (node instanceof TextNode) {
                    TextNode text = (TextNode) node;
                    while (true) {
                        if (i.hasNext()) {
                            Node next = i.next().normalize();
                            if (next instanceof TextNode) {
                                text = text.merge((TextNode) next);
                            } else {
                                if (trimWhitespace) {
                                    String content = text.getContent().trim();
                                    if (content.length() != 0) {
                                        normalizedChildren.add(new TextNode(content));
                                    }
                                } else  {
                                    normalizedChildren.add(text);
                                }
                                normalizedChildren.add(next);
                                break;
                            }
                        } else {
                            if (trimWhitespace) {
                                String content = text.getContent().trim();
                                if (content.length() != 0) {
                                    normalizedChildren.add(new TextNode(content));
                                }
                            } else  {
                                normalizedChildren.add(text);
                            }
                            break;
                        }
                    }
                } else {
                    normalizedChildren.add(node);
                }
            }
            return normalizedChildren;
        }
    }
}
