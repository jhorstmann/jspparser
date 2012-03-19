package net.jhorstmann.jspparser.nodes;

import java.util.LinkedHashMap;
import java.util.Map;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import static net.jhorstmann.jspparser.Constants.*;

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
    public void handle(ContentHandler handler) throws SAXException {
        handler.startDocument();
        handler.startPrefixMapping(PREFIX_JSP, NSURI_JSP);
        if (taglibs != null) {
            for (Map.Entry<String, String> entry : taglibs.entrySet()) {
                handler.startPrefixMapping(entry.getKey(), entry.getValue());
            }
        }
        Attributes attrs = convertAttributes(true);
        handler.startElement(NSURI_JSP, "root", PREFIX_JSP+":root", attrs);
        handleChildren(handler);
        handler.endElement(NSURI_JSP, "root", PREFIX_JSP+":root");
        if (taglibs != null) {
            for (Map.Entry<String, String> entry : taglibs.entrySet()) {
                handler.endPrefixMapping(entry.getKey());
            }
        }
        handler.endPrefixMapping(PREFIX_JSP);
        handler.endDocument();
    }
}
