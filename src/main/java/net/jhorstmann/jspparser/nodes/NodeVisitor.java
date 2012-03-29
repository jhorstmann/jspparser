package net.jhorstmann.jspparser.nodes;

import org.xml.sax.SAXException;

public interface NodeVisitor {
    void visitRoot(RootNode node) throws SAXException;
    void visitComment(CommentNode node) throws SAXException;
    void visitDeclaration(DeclarationNode node) throws SAXException;
    void visitExpression(ExpressionNode node) throws SAXException;
    void visitScriptlet(ScriptletNode node) throws SAXException;
    void visitCustomTag(CustomTagNode node) throws SAXException;
    void visitIncludeDirective(IncludeDirectiveNode node) throws SAXException;
    void visitPageDirective(PageDirectiveNode node) throws SAXException;
    void visitTaglibDirective(TaglibDirectiveNode node) throws SAXException;
    void visitText(TextNode node) throws SAXException;
}
