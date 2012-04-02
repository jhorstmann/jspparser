package net.jhorstmann.jspparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public abstract class AbstractParser {

    private Tokenizer tokenizer;
    private String systemId;
    private String webappRoot;
    private String charset;
    private LinkedList<String> names;
    private Map<String, String> taglibs;
    private LinkedList<String> includes;

    public AbstractParser() {
        this.names = new LinkedList<String>();
        this.taglibs = new HashMap<String, String>();
        this.includes = new LinkedList<String>();
    }

    protected abstract void handleStartTag(String namespaceURI, String localName, String qualifiedName, Map<String, String> attributes) throws SAXException;

    protected abstract void handleEndTag(String namespaceURI, String localName, String qualifiedName) throws SAXException;

    protected abstract void handlePageDirective(Map<String, String> attributes) throws SAXException;
    protected abstract void handleTaglibDirective(Map<String, String> attributes) throws SAXException;
    protected abstract void handleStartIncludeDirective(Map<String, String> attributes) throws SAXException;
    protected abstract void handleEndIncludeDirective() throws SAXException;

    protected abstract void handleTaglib(String prefix, String uri) throws SAXException;

    protected abstract void handleText(String text) throws SAXException;

    protected abstract void handleDeclaration(String text) throws SAXException;

    protected abstract void handleExpression(String text) throws SAXException;

    protected abstract void handleScriptlet(String text) throws SAXException;

    protected abstract void handleComment(String comment) throws SAXException;

    protected abstract void handleStartDocument() throws SAXException;

    protected abstract void handleEndDocument() throws SAXException;

    public final void reset() {
        names.clear();
        taglibs.clear();
        includes.clear();
        charset = null;
    }

    public String getWebappRoot() {
        return webappRoot;
    }

    public void setWebappRoot(String webappRoot) {
        this.webappRoot = webappRoot;
    }

    public void addTaglib(String prefix, String uri) {
        taglibs.put(prefix, uri);
    }

    public void parse(File file, String charset) throws IOException, SAXException {
        this.charset = charset;
        Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
        try {
            parse(reader, file.getAbsolutePath());
        } finally {
            reader.close();
        }
    }

    public void parse(Reader reader) throws IOException, SAXException {
        parse(reader, null);
    }

    public void parse(Reader reader, String systemId) throws IOException, SAXException {
        this.systemId = systemId;
        this.tokenizer = new Tokenizer(reader);
        parsePage();
    }

    public void parse(InputSource input) throws IOException, SAXException {
        String systemId = input.getSystemId();
        Reader characterStream = input.getCharacterStream();
        if (characterStream != null) {
            try {
                parse(characterStream, systemId);
            } finally {
                characterStream.close();
            }
        } else {
            String encoding = input.getEncoding();
            if (encoding == null) {
                encoding = charset != null ? charset : Constants.DEFAULT_CHARSET;
            }
            InputStream byteStream = input.getByteStream();
            if (byteStream != null) {
                try {
                    parse(new InputStreamReader(byteStream, encoding), systemId);
                } finally {
                    byteStream.close();
                }
            } else if (systemId != null) {
                parse(new File(systemId), encoding);
            } else {
                throw new SAXException("Invalid InputSource");
            }
        }
    }

    public void parse(String systemId) throws IOException, SAXException {
        parse(new File(systemId), Constants.DEFAULT_CHARSET);
    }

    void parseTagAttribute(Map<String, String> attributes) throws IOException {
        if (tokenizer.isNameStart()) {
            String name = tokenizer.readName();
            String value;
            tokenizer.skipOptionalSpace();
            tokenizer.consume('=');
            tokenizer.skipOptionalSpace();
            if (tokenizer.isRTAttributeValueDouble()) {
                value = tokenizer.readRTAttributeValueDouble();
            } else if (tokenizer.isAttributeValueDouble()) {
                value = tokenizer.readAttributeValueDouble();
            } else if (tokenizer.isRTAttributeValueSingle()) {
                value = tokenizer.readRTAttributeValueSingle();
            } else if (tokenizer.isAttributeValueSingle()) {
                value = tokenizer.readAttributeValueSingle();
            } else {
                throw new SyntaxException("Invalid attribute value");
            }
            if (attributes.containsKey(name)) {
                throw new SyntaxException("Duplicate attribute name");
            } else {
                attributes.put(name, value);
            }
        } else {
            throw new SyntaxException("Expected tag attribute");
        }
    }

    void parseDirectiveAttribute(Map<String, String> attributes) throws IOException {
        if (tokenizer.isNameStart()) {
            String name = tokenizer.readName();
            String value;
            tokenizer.skipOptionalSpace();
            tokenizer.consume('=');
            tokenizer.skipOptionalSpace();
            if (tokenizer.isAttributeValueDouble()) {
                value = tokenizer.readAttributeValueDouble();
            } else if (tokenizer.isAttributeValueSingle()) {
                value = tokenizer.readAttributeValueSingle();
            } else {
                throw new SyntaxException("Invalid attribute value");
            }
            String prevval = attributes.get(name);
            if (prevval != null) {
                if ("import".equals(name)) {
                    // The 'import' attribute on the 'page' directive is allowed to occur multiple times
                    attributes.put(name, prevval + "," + value);
                } else {
                    throw new SyntaxException("Duplicate attribute name");
                }
            } else {
                attributes.put(name, value);
            }
        } else {
            throw new SyntaxException("Expected directive attribute");
        }
    }

    Map<String, String> parseTagAttributes() throws IOException {
        Map<String, String> attributes = new LinkedHashMap<String, String>();
        while (tokenizer.isSp()) {
            tokenizer.skipRequiredSpace();
            if (tokenizer.isNameStart()) {
                parseTagAttribute(attributes);
            } else {
                break;
            }
        }
        tokenizer.skipOptionalSpace();
        return attributes;
    }

    Map<String, String> parseDirectiveAttributes() throws IOException {
        Map<String, String> attributes = new LinkedHashMap<String, String>();
        while (tokenizer.isSp()) {
            tokenizer.skipRequiredSpace();
            if (tokenizer.isNameStart()) {
                parseDirectiveAttribute(attributes);
            } else {
                break;
            }
        }
        tokenizer.skipOptionalSpace();
        return attributes;
    }

    protected File resolveIncludeFile(String systemId, String include) {
        if (systemId == null) {
            throw new IllegalStateException("Cannot resolve included file with unknown systemId");
        }
        if (include.startsWith("/")) {
            if (webappRoot != null) {
                return new File(webappRoot, include);
            } else {
                return new File(include);
            }
        } else {
            return new File(systemId, include);
        }
    }

    protected InputSource resolveInclude(String systemId, String include) {
        File includeFile = resolveIncludeFile(systemId, include);
        return new InputSource(includeFile.getAbsolutePath());
    }

    private void handleInclude(String file) throws IOException, SAXException {
        if (includes.contains(file)) {
            throw new SyntaxException("Recursive include detected");
        }
        InputSource input = resolveInclude(systemId, file);
        Tokenizer previousTokenizer = tokenizer;
        String previousSystemId = systemId;
        LinkedList<String> previousNames = names;

        includes.addFirst(file);
        try {
            parse(input);
        } finally {
            tokenizer = previousTokenizer;
            systemId = previousSystemId;
            names = previousNames;
            includes.removeFirst();
        }
    }

    void parseDirective() throws IOException, SAXException {
        tokenizer.consume("<%@");
        tokenizer.skipOptionalSpace();
        String name = tokenizer.readName();

        Map<String, String> attrs = parseDirectiveAttributes();

        tokenizer.consume("%>");

        if ("page".equals(name)) {
            if ("true".equals(attrs.get("isELIgnored"))) {
                tokenizer.setIsElEnabled(false);
            } else if ("true".equals(attrs.get("deferredSyntaxAllowedAsLiteral"))) {
            } else if ("true".equals(attrs.get("trimDirectiveWhitespaces"))) {
            }
            String pageEncoding = attrs.get("pageEncoding");
            String contentType = attrs.get("contentType");
            String declaredCharset = null;
            if (pageEncoding != null) {
                declaredCharset = pageEncoding;
            } else if (contentType != null) {
                Matcher matcher = Pattern.compile(";\\s*charset=([a-zA-Z0-9_-]+)").matcher(contentType);
                if (matcher.find()) {
                    declaredCharset = matcher.group(1);
                }
            }
            if (declaredCharset != null && charset != null && !declaredCharset.equalsIgnoreCase(charset)) {
                throw new SyntaxException("Charset declaration does not match, page declared charset " + declaredCharset + " but was opened with " + charset);
            }

            handlePageDirective(attrs);
        } else if ("taglib".equals(name)) {
            String prefix = attrs.get("prefix");
            String uri = attrs.get("uri");
            String tagdir = attrs.get("tagdir");

            if (prefix == null) {
                throw new SyntaxException("Missing taglib prefix");
            }
            if (uri != null) {
                if (tagdir != null) {
                    throw new SyntaxException("Exactly one of 'uri' or 'tagdir' must be specified");
                }
                taglibs.put(prefix, uri);
                handleTaglib(prefix, uri);
            } else if (tagdir != null) {
                handleTaglib(prefix, tagdir);
            } else {
                throw new SyntaxException("Either 'uri' or 'tagdir' must be specified");
            }

            handleTaglibDirective(attrs);
        } else if ("include".equals(name)) {
            String file = attrs.get("file");
            if (file == null) {
                throw new SyntaxException("The file attribute is required for include directive");
            } else {
                handleStartIncludeDirective(attrs);
                handleInclude(file);
                handleEndIncludeDirective();
            }
        }

        if ("page".equals(name)) {
        } else if ("taglib".equals(name)) {
        } else if ("include".equals(name)) {
        }
    }

    public void parseCustomTagOrText() throws IOException, SAXException {
        String tag = tokenizer.readOpenTag();
        int idx = tag.indexOf(':', 1);
        if (idx > 0) {
            String qname = tag.substring(1);
            String prefix = tag.substring(1, idx);
            String name = tag.substring(idx + 1);
            String uri = taglibs.get(prefix);

            if (uri != null) {
                Map<String, String> attrs = parseTagAttributes();
                if (tokenizer.isEndOfEmptyTag()) {
                    tokenizer.skipEndOfEmptyTag();

                    handleStartTag(uri, name, qname, attrs);
                    handleEndTag(uri, name, qname);
                } else if (tokenizer.isEndOfTag()) {
                    tokenizer.skipEndOfTag();
                    names.addFirst(qname);
                    handleStartTag(uri, name, qname, attrs);
                } else {
                    throw new SyntaxException("Expected end of custom tag");
                }
            } else {
                handleText(tag);
            }
        } else {
            handleText(tag);
        }
    }

    public void parseCustomTagEndOrText() throws IOException, SAXException {
        String tag = tokenizer.readCloseTag();
        int idx = tag.indexOf(':', 1);
        if (idx > 0) {
            String qname = tag.substring(2);
            String prefix = tag.substring(2, idx);
            String name = tag.substring(idx + 1);
            String uri = taglibs.get(prefix);
            if (uri != null) {
                tokenizer.skipOptionalSpace();
                if (tokenizer.isEndOfTag()) {
                    tokenizer.skipEndOfTag();
                } else {
                    throw new SyntaxException("Expected end of closing tag");
                }
                if (names.isEmpty()) {
                    throw new SyntaxException("Unexpected close of custom tag");
                } else {
                    String parent = names.removeFirst();
                    if (parent != null && parent.equals(qname)) {
                        handleEndTag(uri, name, qname);
                    } else {
                        throw new SyntaxException("Improper nested custom tags, expected " + parent + " but saw " + qname + " stack=" + names);
                    }
                }
            } else {
                handleText(tag);
            }
        } else {
            handleText(tag);
        }
    }

    void parsePage() throws IOException, SAXException {
        if (includes.isEmpty()) {
            handleStartDocument();
        }
        while (!tokenizer.isEOF()) {
            if (tokenizer.isDirective()) {
                parseDirective();
            } else if (tokenizer.isDeclaration()) {
                String content = tokenizer.readDeclaration();
                handleDeclaration(content);
            } else if (tokenizer.isExpression()) {
                String content = tokenizer.readExpression();
                handleExpression(content);
            } else if (tokenizer.isScriptlet()) {
                String content = tokenizer.readScriptlet();
                handleScriptlet(content);
            } else if (tokenizer.isOpenTag()) {
                parseCustomTagOrText();
            } else if (tokenizer.isCloseTag()) {
                parseCustomTagEndOrText();
            } else if (tokenizer.isTemplateText()) {
                String text = tokenizer.readTemplateText();
                handleText(text);
            } else {
                throw new SyntaxException("Unexpected page element");
            }
        }
        if (includes.isEmpty()) {
            handleEndDocument();
        }
    }
}
