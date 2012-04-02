package net.jhorstmann.jspparser;

import java.io.CharArrayReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;

    public class Tokenizer extends LookAheadReader {

    private boolean isElEnabled;
    private boolean isScriptingEnabled;

    public Tokenizer(char[] data) {
        this(new CharArrayReader(data));
    }

    public Tokenizer(Reader reader) {
        super(reader, 256);
        this.isElEnabled = true;
        this.isScriptingEnabled = true;
    }

    public boolean isIsElEnabled() {
        return isElEnabled;
    }

    public void setIsElEnabled(boolean isElEnabled) {
        this.isElEnabled = isElEnabled;
    }

    public boolean isIsScriptingEnabled() {
        return isScriptingEnabled;
    }

    public void setIsScriptingEnabled(boolean isScriptingEnabled) {
        this.isScriptingEnabled = isScriptingEnabled;
    }

    private static boolean isSpace(int ch) {
        return ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n' || ch == '\f';
    }

    private static boolean isLetter(int ch) {
        // TODO: support proper XML names
        return ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z';
    }

    private static boolean isDigit(int ch) {
        return ch >= '0' && ch <= '9';
    }

    private static boolean isNameStart(int ch) {
        // TODO: this should follow the XML NMTOKEN production
        return isLetter(ch) || ch == '_' || ch == ':';
    }

    private static boolean isNameSubsequent(int ch) {
        return isNameStart(ch) || isDigit(ch) || ch == '.' || ch == '-';
    }

    public void skipOptionalSpace() throws IOException {
        while (isSpace(la1())) {
            read();
        }
    }

    public void skipRequiredSpace() throws IOException {
        int ch = read();
        if (!isSpace(ch)) {
            throw new SyntaxException("Expected white space character");
        }
        skipOptionalSpace();
    }

    public boolean isEOF() throws IOException {
        return la1() == -1;
    }

    public boolean isNameStart() throws IOException {
        return isNameStart(la1());
    }

    public String readName() throws IOException {
        StringBuilder sb = new StringBuilder(16);
        readName(sb);
        return sb.toString();
    }

    private void readName(StringBuilder sb) throws IOException {
        int ch = read();
        if (!isNameStart(ch)) {
            throw new SyntaxException();
        }
        sb.append((char) ch);
        while (true) {
            ch = la1();
            if (isNameSubsequent(ch)) {
                read();
                sb.append((char) ch);
            } else {
                break;
            }
        }
    }

    public boolean isScriptEnd() throws IOException {
        return lookingAt("%>");
    }

    public boolean isDeclaration() throws IOException {
        return lookingAt("<%!");
    }

    public boolean isExpression() throws IOException {
        return lookingAt("<%=");
    }

    public boolean isScriptlet() throws IOException {
        return lookingAt("<%");
    }

    public boolean isComment() throws IOException {
        return lookingAt("<%--");
    }

    public boolean isRTAttributeValueDouble() throws IOException {
        return lookingAt("\"<%=");
    }

    public boolean isRTAttributeValueSingle() throws IOException {
        return lookingAt("'<%=");
    }

    public boolean isAttributeValueDouble() throws IOException {
        return la1() == '"';
    }

    public boolean isAttributeValueSingle() throws IOException {
        return la1() == '\'';
    }

    public boolean isEq() throws IOException {
        return la1() == '=';
    }

    public boolean isSp() throws IOException {
        return isSpace(la1());
    }

    public boolean isOpenTag() throws IOException {
        int ch = la1();
        if (ch == '<') {
            ch = la2();
            return isNameStart(ch);
        } else {
            return false;
        }
    }

    public boolean isCloseTag() throws IOException {
        int ch = la1();
        if (ch == '<') {
            ch = la2();
            return ch == '/' && isNameStart(la3());
        } else {
            return false;
        }
    }

    public boolean isEndOfTag() throws IOException {
        return la1() == '>';
    }

    public void skipEndOfTag() throws IOException {
        consume('>');
    }

    public boolean isEndOfEmptyTag() throws IOException {
        return la1() == '/';
    }

    public void skipEndOfEmptyTag() throws IOException {
        consume("/>");
    }

    public String readOpenTag() throws IOException {
        consume('<');
        StringBuilder sb = new StringBuilder(32);
        sb.append('<');
        readName(sb);
        return sb.toString();
    }

    public String readCloseTag() throws IOException {
        consume("</");
        StringBuilder sb = new StringBuilder(32);
        sb.append('<');
        sb.append('/');
        readName(sb);
        return sb.toString();
    }

    private String readScriptContent() throws IOException {
        StringBuilder sb = new StringBuilder(64);
        while (true) {
            int ch = read();
            if (ch == -1) {
                throw new EOFException("EOF");
            } else if (ch == '%' && la1() == '>') {
                read();
                break;
            } else {
                sb.append((char) ch);
            }
        }
        return sb.toString();
    }

    public String readDeclaration() throws IOException {
        consume("<%!");
        return readScriptContent();
    }

    public String readExpression() throws IOException {
        consume("<%=");
        return readScriptContent();
    }

    public String readScriptlet() throws IOException {
        consume("<%");
        return readScriptContent();
    }

    public String readComment() throws IOException {
        consume("<%--");
        StringBuilder sb = new StringBuilder();
        while (true) {
            int ch = read();
            if (ch == -1) {
                throw new EOFException("EOF");
            } else if (ch == '-') {
                ch = read();
                if (ch == -1) {
                    throw new EOFException("EOF");
                } else if (ch == '-') {
                    ch = read();
                    if (ch == -1) {
                        throw new EOFException("EOF");
                    } else if (ch == '%') {
                        ch = read();
                        if (ch == -1) {
                            throw new EOFException("EOF");
                        } else if (ch == '>') {
                            break;
                        } else {
                            sb.append('-');
                            sb.append('-');
                            sb.append('%');
                            sb.append((char) ch);
                        }
                    } else {
                        sb.append('-');
                        sb.append('-');
                        sb.append((char) ch);
                    }
                } else {
                    sb.append('-');
                    sb.append((char) ch);
                }
            } else {
                sb.append((char) ch);
            }
        }
        return sb.toString();
    }

    public boolean isTemplateText() throws IOException {
        int ch = la1();
        if (ch == -1) {
            return false;
        } else if (ch == '<') {
            ch = la2();
            return (ch != '\\' && ch != '%' && !isNameStart(ch));
        } else if (isElEnabled && ch == '\\') {
            ch = la2();
            return ch != '$' && ch != '#';
        } else if (isElEnabled && (ch == '$' || ch == '#')) {
            ch = la2();
            return ch != '{';
        } else {
            return true;
        }
    }

    public String readQuotedStringEL() throws IOException {
        int ch = read();
        if (ch == '\'' || ch == '"') {
            return (char)ch + readQuotedStringEL(ch);
        } else {
            throw new SyntaxException();
        }
    }
    
    private String readQuotedStringEL(int quote) throws IOException {
        StringBuilder sb = new StringBuilder();
        while (true) {
            int ch = read();
            if (ch == -1) {
                throw new EOFException("EOF");
            } else if (ch == quote) {
                sb.append((char)ch);
                break;
            } else if (ch == '\\') {
                ch = read();
                if (ch == -1) {
                    throw new java.io.EOFException("EOF");
                } else if (ch == quote || ch == '\\') {
                    sb.append('\\');
                    sb.append((char)ch);
                } else {
                    // TODO: According to the EL grammar this should be invalid
                    sb.append('\\');
                    sb.append((char)ch);
                }
            } else {
                sb.append((char)ch);
            }
        }
        return sb.toString();
    }

    public String readTemplateTextEL() throws IOException {
        int ch = read();
        if (ch != '$' && ch != '#') {
            throw new SyntaxException("Expected EL expression");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append((char) ch);
            ch = read();
            if (ch != '{') {
                throw new SyntaxException("Expected EL expression");
            } else {
                sb.append((char) ch);
                while (true) {
                    ch = read();
                    if (ch == -1) {
                        throw new EOFException("EOF");
                    } else if (ch == '}') {
                        sb.append((char)ch);
                        break;
                    } else if (ch == '\'' || ch == '"') {
                        sb.append((char)ch);
                        sb.append(readQuotedStringEL(ch));
                    } else {
                        sb.append((char)ch);
                    }
                }
            }
            return sb.toString();
        }
    }

    public String readTemplateText() throws IOException {
        StringBuilder sb = new StringBuilder();
        while (true) {
            int ch = la1();
            if (ch == -1) {
                break;
            } else if (ch == '<') {
                ch = la2();
                if (ch == '\\' && la3() == '%') {
                    read();
                    read();
                    read();
                    sb.append('<');
                    sb.append('%');
                } else if (ch == '%' || ch == '/' || isNameStart(ch)) {
                    break;
                } else {
                    read();
                    read();
                    sb.append('<');
                    sb.append((char)ch);
                }
            } else if (isElEnabled && (ch == '$' || ch == '#') && la2() == '{') {
                sb.append(readTemplateTextEL());
            } else if (isElEnabled && ch == '\\') {
                ch = la2();
                if (ch == '$' || ch == '#') {
                    read();
                    read();
                    sb.append('\\');
                    sb.append((char)ch);
                } else {
                    sb.append('\\');
                }
            } else {
                read();
                sb.append((char) ch);
            }
        }

        return sb.toString();
    }

    String readAttributeValue(char quote, boolean isrt) throws IOException {
        if (isrt && !isScriptingEnabled) {
            throw new SyntaxException("Scripting is disabled for this page");
        }
        StringBuilder sb = new StringBuilder();
        while (true) {
            int ch = la1();
            if (ch == -1 || ch == quote) {
                break;
            } else if (ch == '%') {
                ch = la2();
                if (isrt && ch == '>') {
                    break;
                } else if (ch == '\\') {
                    ch = la3();
                    if (ch == '>') {
                        read();
                        read();
                        read();
                        sb.append('%');
                        sb.append('>');
                    } else {
                        read();
                        read();
                        sb.append('%');
                        sb.append('\\');
                    }
                }
                break;
            } else if (ch == '<') {
                ch = la2();
                if (ch == '\\') {
                    ch = la3();
                    if (ch == '%') {
                        read();
                        read();
                        read();
                        sb.append('<');
                        sb.append('%');
                    } else {
                        read();
                        sb.append('<');
                        sb.append('\\');
                    }
                } else {
                    read();
                    sb.append('<');
                }
            } else if (ch == '\\') {
                ch = la2();
                if (ch == '\\' || ch == '"' || ch == '\'' || (isElEnabled && (ch == '$' || ch == '#'))) {
                    read();
                    read();
                    sb.append((char) ch);
                } else {
                    read();
                    sb.append('\\');
                    sb.append((char) ch);
                }
            } else if (ch == '&') {
                if (lookingAt("&apos;")) {
                    consume("&apos;");
                    sb.append('\'');
                } else if (lookingAt("&quot;")) {
                    consume("&quot;");
                    sb.append('"');
                } else {
                    read();
                    sb.append('&');
                }
            } else {
                read();
                sb.append((char) ch);
            }
        }
        return sb.toString();
    }

    public String readRTAttributeValueDouble() throws IOException {
        consume("\"<%=");
        String value = readAttributeValue('"', true);
        consume("%>\"");
        return "<%=" + value + "%>";
    }

    public String readAttributeValueDouble() throws IOException {
        consume('"');
        String value = readAttributeValue('"', false);
        consume('"');
        return value;
    }

    public String readRTAttributeValueSingle() throws IOException {
        consume("'<%=");
        String value = readAttributeValue('\'', true);
        consume("%>'");
        return "<%=" + value + "%>";
    }

    public String readAttributeValueSingle() throws IOException {
        consume('\'');
        String value = readAttributeValue('\'', false);
        consume('\'');
        return value;
    }

    public boolean isDirective() throws IOException {
        return lookingAt("<%@");
    }
}
