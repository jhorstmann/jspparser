package net.jhorstmann.jspparser.nodes;

import java.util.LinkedHashMap;
import java.util.Map;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

public abstract class AttributedNode extends Node {

    protected Map<String, String> attributes;

    public AttributedNode() {
    }

    public AttributedNode(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public void addAttribute(String name, String value) {
        getAttributes().put(name, value);
    }

    public void addAttributes(Map<String, String> attrs) {
        getAttributes().putAll(attrs);
    }

    public Map<String, String> getAttributes() {
        if (attributes == null) {
            attributes = new LinkedHashMap<String, String>();
        }
        return attributes;
    }

    public Attributes getAttributes(boolean convertScriptExpressions) {
        AttributesImpl attrs = new AttributesImpl();
        if (attributes != null) {
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                String name = entry.getKey();
                String content = entry.getValue();
                if (convertScriptExpressions && content.startsWith("<%=")) {
                    if (content.endsWith("%>")) {
                        content = content.substring(1, content.length() - 1);
                    } else {
                        throw new IllegalStateException("Invalid script expression value");
                    }
                }
                attrs.addAttribute("", name, name, "CDATA", content);
            }
        }
        return attrs;
    }

    protected Map<String, String> normalizeAttributes() {
        return attributes == null ? null : new LinkedHashMap<String, String>(attributes);
    }
}
