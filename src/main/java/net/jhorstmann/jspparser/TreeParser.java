package net.jhorstmann.jspparser;

import java.util.LinkedList;
import java.util.Map;
import net.jhorstmann.jspparser.nodes.CommentNode;
import net.jhorstmann.jspparser.nodes.CustomTagNode;
import net.jhorstmann.jspparser.nodes.DeclarationNode;
import net.jhorstmann.jspparser.nodes.ExpressionNode;
import net.jhorstmann.jspparser.nodes.IncludeDirectiveNode;
import net.jhorstmann.jspparser.nodes.Node;
import net.jhorstmann.jspparser.nodes.PageDirectiveNode;
import net.jhorstmann.jspparser.nodes.ParentNode;
import net.jhorstmann.jspparser.nodes.RootNode;
import net.jhorstmann.jspparser.nodes.ScriptletNode;
import net.jhorstmann.jspparser.nodes.TaglibDirectiveNode;
import net.jhorstmann.jspparser.nodes.TextNode;
import org.xml.sax.SAXException;

public class TreeParser extends AbstractParser {

    private RootNode root;
    private LinkedList<ParentNode> stack;

    public TreeParser() {
        this.root = new RootNode();
        this.stack = new LinkedList<ParentNode>();
        this.stack.addFirst(root);
    }

    public RootNode getRootNode() {
        return root;
    }

    public RootNode getNormalizedRootNode() {
        return (RootNode) root.normalize();
    }

    Node getFirstChild() {
        return root.getChildNodes().get(0);
    }

    private void pushNode(ParentNode parent) {
        stack.addFirst(parent);
    }

    private ParentNode popNode() {
        return stack.removeFirst();
    }

    private ParentNode topNode() {
        return stack.getFirst();
    }

    @Override
    protected void handleStartDocument() throws SAXException {
    }

    @Override
    protected void handleEndDocument() throws SAXException {
    }

    @Override
    protected void handleTaglib(String prefix, String uri) {
        root.addTaglib(prefix, uri);
    }

    @Override
    protected void handleStartIncludeDirective(Map<String, String> attributes) throws SAXException {
        IncludeDirectiveNode directive = new IncludeDirectiveNode(attributes);

        topNode().addChildNode(directive);
        pushNode(directive);
    }

    @Override
    protected void handleEndIncludeDirective() throws SAXException {
        popNode();
    }

    @Override
    protected void handlePageDirective(Map<String, String> attributes) throws SAXException {
        PageDirectiveNode directive = new PageDirectiveNode(attributes);
        topNode().addChildNode(directive);
    }

    @Override
    protected void handleTaglibDirective(Map<String, String> attributes) throws SAXException {
        TaglibDirectiveNode directive = new TaglibDirectiveNode(attributes);
        topNode().addChildNode(directive);
    }

    @Override
    protected void handleDeclaration(String text) {
        DeclarationNode decl = new DeclarationNode(text);
        topNode().addChildNode(decl);
    }

    @Override
    protected void handleExpression(String text) {
        ExpressionNode expr = new ExpressionNode(text);
        topNode().addChildNode(expr);
    }

    @Override
    protected void handleScriptlet(String text) {
        ScriptletNode script = new ScriptletNode(text);
        topNode().addChildNode(script);
    }

    @Override
    protected void handleStartTag(String namespaceURI, String localName, String qualifiedName, Map<String, String> attributes) {
        CustomTagNode elem = new CustomTagNode(localName, namespaceURI, qualifiedName, attributes);
        topNode().addChildNode(elem);
        pushNode(elem);
    }

    @Override
    protected void handleEndTag(String namespaceURI, String localName, String qualifiedName) {
        popNode();
    }

    @Override
    protected void handleComment(String comment) throws SAXException {
        topNode().addChildNode(new CommentNode(comment));
    }

    @Override
    protected void handleText(String text) {
        topNode().addChildNode(new TextNode(text));
    }
}
