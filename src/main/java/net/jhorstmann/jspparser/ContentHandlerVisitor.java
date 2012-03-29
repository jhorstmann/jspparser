package net.jhorstmann.jspparser;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import net.jhorstmann.jspparser.nodes.CommentNode;
import net.jhorstmann.jspparser.nodes.CustomTagNode;
import net.jhorstmann.jspparser.nodes.DeclarationNode;
import net.jhorstmann.jspparser.nodes.ExpressionNode;
import net.jhorstmann.jspparser.nodes.IncludeDirectiveNode;
import net.jhorstmann.jspparser.nodes.NodeVisitor;
import net.jhorstmann.jspparser.nodes.PageDirectiveNode;
import net.jhorstmann.jspparser.nodes.RootNode;
import net.jhorstmann.jspparser.nodes.ScriptletNode;
import net.jhorstmann.jspparser.nodes.TaglibDirectiveNode;
import net.jhorstmann.jspparser.nodes.TextNode;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import static net.jhorstmann.jspparser.Constants.*;
import net.jhorstmann.jspparser.nodes.Node;
import net.jhorstmann.jspparser.nodes.ParentNode;
import net.jhorstmann.jspparser.nodes.ScriptNode;
import org.xml.sax.ext.LexicalHandler;

public class ContentHandlerVisitor implements NodeVisitor {
    public static enum Flags {
        FOLLOW_INCLUDES, HANDLE_INCLUDE_DIRECTIVE, HANDLE_PAGE_DIRECTIVE, HANDLE_TAGLIB_DIRECTIVE, HANDLE_COMMENT, WRAP_TEXT;

        public static Flags[] handleAll() {
            Flags[] flags = {HANDLE_COMMENT, HANDLE_INCLUDE_DIRECTIVE, HANDLE_PAGE_DIRECTIVE, HANDLE_TAGLIB_DIRECTIVE};
            return flags.clone();
        }

        public static Flags[] noTextWrap() {
            Flags[] flags = {HANDLE_COMMENT, HANDLE_INCLUDE_DIRECTIVE, HANDLE_PAGE_DIRECTIVE, HANDLE_TAGLIB_DIRECTIVE, FOLLOW_INCLUDES};
            return flags.clone();
        }
    }

    private final ContentHandler handler;
    private final EnumSet<Flags> flags;

    public ContentHandlerVisitor(ContentHandler handler, Flags... flags) {
        this.handler = handler;
        this.flags = EnumSet.noneOf(Flags.class);
        this.flags.addAll(Arrays.asList(flags));
    }

    void handleChildren(ParentNode node) throws SAXException {
        for (Node child : node.getChildNodes()) {
            child.accept(this);
        }
    }

    @Override
    public void visitRoot(RootNode node) throws SAXException {
        handler.startDocument();
        handler.startPrefixMapping(PREFIX_JSP, NSURI_JSP);
        Map<String, String> taglibs = node.getTaglibs();
        if (taglibs != null) {
            for (Map.Entry<String, String> entry : node.getTaglibs().entrySet()) {
                handler.startPrefixMapping(entry.getKey(), entry.getValue());
            }
        }
        Attributes attrs = node.getAttributes(true);
        handler.startElement(NSURI_JSP, "root", PREFIX_JSP + ":root", attrs);
        handleChildren(node);
        handler.endElement(NSURI_JSP, "root", PREFIX_JSP + ":root");
        if (taglibs != null) {
            for (Map.Entry<String, String> entry : taglibs.entrySet()) {
                handler.endPrefixMapping(entry.getKey());
            }
        }
        handler.endPrefixMapping(PREFIX_JSP);
        handler.endDocument();
    }

    @Override
    public void visitComment(CommentNode node) throws SAXException {
        if (flags.contains(Flags.HANDLE_COMMENT) && handler instanceof LexicalHandler) {
            LexicalHandler lexicalHandler = (LexicalHandler) handler;
            String content = node.getContent();
            if (content.length() > 0) {
                char[] chars = content.toCharArray();
                lexicalHandler.comment(chars, 0, chars.length);
            }
        }
    }

    void visitScriptNode(String name, ScriptNode node) throws SAXException {
        handler.startElement(NSURI_JSP, name, PREFIX_JSP + ":" + name, EMPTY_ATTRS);
        String content = node.getContent();
        if (content != null) {
            char[] data = content.toCharArray();
            handler.characters(data, 0, data.length);
        }
        handler.endElement(NSURI_JSP, name, PREFIX_JSP + ":" + name);
    }

    @Override
    public void visitDeclaration(DeclarationNode node) throws SAXException {
        visitScriptNode("declaration", node);
    }

    @Override
    public void visitExpression(ExpressionNode node) throws SAXException {
        visitScriptNode("expression", node);
    }

    @Override
    public void visitScriptlet(ScriptletNode node) throws SAXException {
        visitScriptNode("scriptlet", node);
    }

    @Override
    public void visitCustomTag(CustomTagNode node) throws SAXException {
        Attributes attrs = node.getAttributes(true);
        String namespaceURI = node.getNamespaceURI();
        String localName = node.getLocalName();
        String qualifiedName = node.getQualifiedName();
        handler.startElement(namespaceURI, localName, qualifiedName, attrs);
        handleChildren(node);
        handler.endElement(namespaceURI, localName, qualifiedName);
    }

    @Override
    public void visitIncludeDirective(IncludeDirectiveNode node) throws SAXException {
        if (flags.contains(Flags.HANDLE_INCLUDE_DIRECTIVE)) {
            String dirname = "directive.include";
            Attributes attrs = node.getAttributes(true);
            handler.startElement(NSURI_JSP, dirname, PREFIX_JSP + ":" + dirname, attrs);
            if (flags.contains(Flags.FOLLOW_INCLUDES)) {
                handleChildren(node);
            }
            handler.endElement(NSURI_JSP, dirname, PREFIX_JSP + ":" + dirname);
        } else if (flags.contains(Flags.FOLLOW_INCLUDES)) {
            handleChildren(node);
        }
    }

    @Override
    public void visitPageDirective(PageDirectiveNode node) throws SAXException {
        if (flags.contains(Flags.HANDLE_PAGE_DIRECTIVE)) {
            String dirname = "directive.page";
            Attributes attrs = node.getAttributes(true);
            handler.startElement(NSURI_JSP, dirname, PREFIX_JSP + ":" + dirname, attrs);
            handler.endElement(NSURI_JSP, dirname, PREFIX_JSP + ":" + dirname);
        }
    }

    @Override
    public void visitTaglibDirective(TaglibDirectiveNode node) throws SAXException {
        if (flags.contains(Flags.HANDLE_TAGLIB_DIRECTIVE)) {
            String dirname = "directive.taglib";
            Attributes attrs = node.getAttributes(true);
            handler.startElement(NSURI_JSP, dirname, PREFIX_JSP + ":" + dirname, attrs);
            handler.endElement(NSURI_JSP, dirname, PREFIX_JSP + ":" + dirname);
        }
    }

    @Override
    public void visitText(TextNode node) throws SAXException {
        String content = node.getContent();
        if (content.length() > 0) {
            boolean wrapText = flags.contains(Flags.WRAP_TEXT);
            if (wrapText) {
                handler.startElement(NSURI_JSP, "text", PREFIX_JSP + ":text", EMPTY_ATTRS);
            }
            char[] chars = content.toCharArray();
            handler.characters(chars, 0, chars.length);
            if (wrapText) {
                handler.endElement(NSURI_JSP, "text", PREFIX_JSP + ":text");
            }
        }
    }
}
