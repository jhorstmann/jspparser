package net.jhorstmann.jspparser;

import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

public class JSPXMLReader extends StreamingParser implements XMLReader {
    private EntityResolver resolver;
    private LexicalHandler lexicalHandler;

    public LexicalHandler getLexicalHandler() {
        return lexicalHandler;
    }

    public void setLexicalHandler(LexicalHandler lexicalHandler) {
        this.lexicalHandler = lexicalHandler;
    }
    
    @Override
    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        if ("http://xml.org/sax/features/namespaces".equals(name)) {
            return true;
        } else if ("http://xml.org/sax/features/namespace-prefixes".equals(name)) {
            return false;
        } else {
            throw new SAXNotRecognizedException("Feature " + name);
        }
    }

    @Override
    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
        if ("http://xml.org/sax/features/namespaces".equals(name)) {
            if (!value) {
                throw new SAXNotSupportedException();
            }
        } else if ("http://xml.org/sax/features/namespace-prefixes".equals(name)) {
            if (value) {
                throw new SAXNotSupportedException();
            }
        } else {
            throw new SAXNotRecognizedException("Feature " + name);
        }
    }

    @Override
    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        if ("http://xml.org/sax/properties/lexical-handler".equals(name)) {
            return lexicalHandler;
        } else {
            throw new SAXNotRecognizedException("Property " + name);
        }
    }

    @Override
    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        if ("http://xml.org/sax/properties/lexical-handler".equals(name)) {
            setLexicalHandler((LexicalHandler)value);
        } else {
            throw new SAXNotRecognizedException("Property " + name);
        }
    }

    @Override
    public void setEntityResolver(EntityResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public EntityResolver getEntityResolver() {
        return resolver;
    }

    @Override
    public void setDTDHandler(DTDHandler handler) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DTDHandler getDTDHandler() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setErrorHandler(ErrorHandler handler) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ErrorHandler getErrorHandler() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void handleComment(String comment) throws SAXException {
        if (lexicalHandler != null) {
            HandlerUtil.handleComment(lexicalHandler, comment);
        }
    }
}
