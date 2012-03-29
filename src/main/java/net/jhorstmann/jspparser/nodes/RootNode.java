package net.jhorstmann.jspparser.nodes;

import java.util.LinkedHashMap;
import java.util.Map;
import org.xml.sax.SAXException;


public class RootNode extends ParentNode {
    private Map<String, String> taglibs;

    @Override
    public Node normalize(boolean trimWhitespace) {
        RootNode root = new RootNode();
        root.children = normalizeChildren(trimWhitespace);
        root.attributes = normalizeAttributes();
        return root;
    }
    
    public void addTaglib(String prefix, String uri) {
        if (taglibs == null) {
            taglibs = new LinkedHashMap<String, String>();
        } else {
            String olduri = taglibs.get(prefix);
            if (olduri != null && !olduri.equals(uri)) {
                throw new IllegalStateException("Taglib for prefix '" + prefix + "' already defined to '" + olduri + "'");
            }
        }
        taglibs.put(prefix, uri);
    }

    public String getTaglibUri(String prefix) {
        return taglibs == null ? null : taglibs.get(prefix);
    }

    public boolean isTaglibPrefix(String prefix) {
        return taglibs != null && taglibs.containsKey(prefix);
    }

    public Map<String, String> getTaglibs() {
        return taglibs;
    }

    @Override
    public void accept(NodeVisitor visitor) throws SAXException {
        visitor.visitRoot(this);
    }
}
