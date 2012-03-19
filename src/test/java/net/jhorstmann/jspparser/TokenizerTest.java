package net.jhorstmann.jspparser;

import java.io.IOException;
import java.io.StringReader;
import org.junit.Assert;
import org.junit.Test;

public class TokenizerTest {

    @Test
    public void testAttributeValue() throws IOException {
        Assert.assertEquals("xyz", new Tokenizer(new StringReader("'xyz'")).readAttributeValueSingle());
        Assert.assertEquals("xyz", new Tokenizer(new StringReader("\"xyz\"")).readAttributeValueDouble());
    }

    @Test
    public void testAttributeValueRT() throws IOException {
        Assert.assertEquals("<%=xyz%>", new Tokenizer(new StringReader("'<%=xyz%>'")).readRTAttributeValueSingle());
        Assert.assertEquals("<%=xyz%>", new Tokenizer(new StringReader("\"<%=xyz%>\"")).readRTAttributeValueDouble());
    }

    @Test
    public void testAttributeValueEL() throws IOException {
        Assert.assertEquals("${\"test\"}", new Tokenizer(new StringReader("'${\"test\"}'")).readAttributeValueSingle());
        Assert.assertEquals("${'}'}", new Tokenizer(new StringReader("\"${'}'}\"")).readAttributeValueDouble());
    }

    @Test(expected = SyntaxException.class)
    public void testInvalidAttributeValueBug45015a() throws IOException {
        new Tokenizer(new StringReader("\"<%= \"hi!\" %>\"")).readRTAttributeValueDouble();
    }

    @Test
    public void testInvalidAttributeValueBug45015b() throws IOException {
        Assert.assertEquals("<%= \"hi!\" %>", new Tokenizer(new StringReader("'<%= \"hi!\" %>'")).readRTAttributeValueSingle());
    }

    @Test
    public void testInvalidAttributeValueBug45015c() throws IOException {
        Assert.assertEquals("<%= \"hi!\" %>", new Tokenizer(new StringReader("\"<%= \\\"hi!\\\" %>\"")).readRTAttributeValueDouble());
    }

    @Test
    public void testInvalidAttributeValueBug45015d() throws IOException {
        Assert.assertEquals("<%= \"hi!\" %>", new Tokenizer(new StringReader("'<%= \\\"hi!\\\" %>'")).readRTAttributeValueSingle());
    }

    @Test
    public void testUnmatchedQuotesBug45427a() throws IOException {
        Assert.assertEquals("${'This string contains unmatched escaped \\' single and \" double quotes, inside single quotes'}", new Tokenizer(new StringReader("${'This string contains unmatched escaped \\' single and \" double quotes, inside single quotes'}")).readTemplateText());
        Assert.assertEquals("${\"This string contains unmatched ' single and escaped \\\" double quotes, inside double quotes\"}", new Tokenizer(new StringReader("${\"This string contains unmatched ' single and escaped \\\" double quotes, inside double quotes\"}")).readTemplateText());
        Assert.assertEquals("${\"This string contains an ' unescaped single quote, inside double quotes\"}", new Tokenizer(new StringReader("${\"This string contains an ' unescaped single quote, inside double quotes\"}")).readTemplateText());
        Assert.assertEquals("${'This string contains an \" unescaped double quote, inside single quotes'}", new Tokenizer(new StringReader("${'This string contains an \" unescaped double quote, inside single quotes'}")).readTemplateText());
    }

    @Test
    public void testSingleQuotedStringEL() throws IOException {
        Assert.assertEquals("'test'", new Tokenizer(new StringReader("'test'")).readQuotedStringEL());
        Assert.assertEquals("'\\''", new Tokenizer(new StringReader("'\\''")).readQuotedStringEL());
        Assert.assertEquals("'\\\\'", new Tokenizer(new StringReader("'\\\\'")).readQuotedStringEL());
        Assert.assertEquals("'\"'", new Tokenizer(new StringReader("'\"'")).readQuotedStringEL());
    }

    @Test
    public void testDirective() throws IOException {
        Tokenizer t = new Tokenizer(new StringReader("<%@page encoding='utf-8' %>"));
        Assert.assertTrue(t.isDirective());
        t.consume("<%@");
        t.skipOptionalSpace();
        String page = t.readName();
        Assert.assertEquals("page", page);
        Assert.assertTrue(t.isSp());
        t.skipRequiredSpace();
        String enc = t.readName();
        Assert.assertEquals("encoding", enc);
        Assert.assertTrue(t.isEq());
        t.skipOptionalSpace();
        t.consume('=');
        Assert.assertTrue(t.isAttributeValueSingle());
        t.skipOptionalSpace();
        String utf8 = t.readAttributeValueSingle();
        Assert.assertEquals("utf-8", utf8);
        Assert.assertTrue(t.isSp());
        t.skipOptionalSpace();
        t.consume("%>");
        Assert.assertTrue(t.isEOF());
    }

    @Test
    public void testDirectives() throws IOException {
        Tokenizer t = new Tokenizer(new StringReader("<%@page %>\r\n<%@taglib %>"));
        Assert.assertTrue(t.isDirective());
        t.consume("<%@");
        t.skipOptionalSpace();
        String page = t.readName();
        Assert.assertEquals("page", page);
        t.skipOptionalSpace();
        t.consume("%>");
        Assert.assertTrue(t.isTemplateText());
        String text = t.readTemplateText();
        Assert.assertEquals("\r\n", text);
        Assert.assertTrue(t.isDirective());
        t.consume("<%@");
        t.skipOptionalSpace();
        String taglib = t.readName();
        Assert.assertEquals("taglib", taglib);
        t.skipOptionalSpace();
        t.consume("%>");
        Assert.assertTrue(t.isEOF());
    }

    @Test
    public void testTemplateText() throws IOException {
        Tokenizer t = new Tokenizer(new StringReader("Hello World"));
        Assert.assertTrue(t.isTemplateText());
        String text = t.readTemplateText();
        Assert.assertEquals("Hello World", text);
        Assert.assertTrue(t.isEOF());
    }

    @Test
    public void testTemplateText2() throws IOException {
        Tokenizer t = new Tokenizer(new StringReader("Hello--World"));
        Assert.assertTrue(t.isTemplateText());
        String text = t.readTemplateText();
        Assert.assertEquals("Hello--World", text);
        Assert.assertTrue(t.isEOF());
    }

    @Test
    public void testTemplateText3() throws IOException {
        Tokenizer t = new Tokenizer(new StringReader("Hello%>World"));
        Assert.assertTrue(t.isTemplateText());
        String text = t.readTemplateText();
        Assert.assertEquals("Hello%>World", text);
        Assert.assertTrue(t.isEOF());
    }

    @Test
    public void testTemplateText4() throws IOException {
        Tokenizer t = new Tokenizer(new StringReader("Hello World<%"));
        Assert.assertTrue(t.isTemplateText());
        String text = t.readTemplateText();
        Assert.assertEquals("Hello World", text);
        Assert.assertTrue(t.isScriptlet());
        t.consume("<%");
        Assert.assertTrue(t.isEOF());
    }

    @Test
    public void testTemplateTextWithEscapedScriptlet() throws IOException {
        Tokenizer t = new Tokenizer(new StringReader("Hello<\\%World"));
        Assert.assertTrue(t.isTemplateText());
        String text = t.readTemplateText();
        Assert.assertEquals("Hello<%World", text);
        Assert.assertTrue(t.isEOF());
    }

    @Test
    public void testTemplateTextWithEscapedEL() throws IOException {
        {
            Tokenizer t = new Tokenizer(new StringReader("Hello\\${World"));
            t.setIsElEnabled(false);
            Assert.assertTrue(t.isTemplateText());
            String text = t.readTemplateText();
            Assert.assertEquals("Hello\\${World", text);
            Assert.assertTrue(t.isEOF());
        }
        {
            Tokenizer t = new Tokenizer(new StringReader("Hello\\${'<x'}World"));
            t.setIsElEnabled(false);
            Assert.assertTrue(t.isTemplateText());
            String text = t.readTemplateText();
            Assert.assertEquals("Hello\\${'", text);
            Assert.assertTrue(t.isOpenTag());
            String tag = t.readOpenTag();
            Assert.assertEquals("<x", tag);
            Assert.assertTrue(t.isTemplateText());
            String text2 = t.readTemplateText();
            Assert.assertEquals("'}World", text2);
            Assert.assertTrue(t.isEOF());
        }
    }

    @Test
    public void testTemplateTextWithEnabledEL() throws IOException {
        {
            Tokenizer t = new Tokenizer(new StringReader("Hello\\${World"));
            t.setIsElEnabled(true);
            Assert.assertTrue(t.isTemplateText());
            String text = t.readTemplateText();
            Assert.assertEquals("Hello\\${World", text);
            Assert.assertTrue(t.isEOF());
        }
        {
            Tokenizer t = new Tokenizer(new StringReader("Hello${'<x'}World"));
            t.setIsElEnabled(true);
            Assert.assertTrue(t.isTemplateText());
            String text = t.readTemplateText();
            Assert.assertEquals("Hello${'<x'}World", text);
            Assert.assertTrue(t.isEOF());
        }
    }

    @Test
    public void testTemplateTextWithEL() throws IOException {
        Tokenizer t = new Tokenizer(new StringReader("Hello${test}World"));
        t.setIsElEnabled(true);
        Assert.assertTrue(t.isTemplateText());
        String text = t.readTemplateText();
        Assert.assertEquals("Hello${test}World", text);
        Assert.assertTrue(t.isEOF());
    }

    @Test(expected = EOFException.class)
    public void testTemplateTextWithUnclosedEnabledEL() throws IOException {
        Tokenizer t = new Tokenizer(new StringReader("Hello${test"));
        t.setIsElEnabled(true);
        Assert.assertTrue(t.isTemplateText());
        String text = t.readTemplateText();
    }

    @Test
    public void testTemplateTextWithUnclosedEnabledEscapedEL() throws IOException {
        Tokenizer t = new Tokenizer(new StringReader("Hello\\${test"));
        t.setIsElEnabled(true);
        Assert.assertTrue(t.isTemplateText());
        String text = t.readTemplateText();
        Assert.assertEquals("Hello\\${test", text);
        Assert.assertTrue(t.isEOF());
    }

    @Test
    public void testTemplateTextWithUnclosedEscapedEL() throws IOException {
        Tokenizer t = new Tokenizer(new StringReader("Hello\\${test"));
        t.setIsElEnabled(false);
        Assert.assertTrue(t.isTemplateText());
        String text = t.readTemplateText();
        Assert.assertEquals("Hello\\${test", text);
        Assert.assertTrue(t.isEOF());
    }

    @Test(expected = EOFException.class)
    public void testTemplateTextWithUnclosedEL() throws IOException {
        Tokenizer t = new Tokenizer(new StringReader("Hello${test"));
        t.setIsElEnabled(true);
        Assert.assertTrue(t.isTemplateText());
        String text = t.readTemplateText();
    }

    @Test
    public void testComment() throws IOException {
        Tokenizer t = new Tokenizer(new StringReader("<%-- comment --%>"));
        Assert.assertTrue(t.isComment());
        String comment = t.readComment();
        Assert.assertEquals(" comment ", comment);
        Assert.assertTrue(t.isEOF());
    }

    @Test
    public void testComment2() throws IOException {
        Tokenizer t = new Tokenizer(new StringReader("<%-- comment--test --%>"));
        Assert.assertTrue(t.isComment());
        String comment = t.readComment();
        Assert.assertEquals(" comment--test ", comment);
        Assert.assertTrue(t.isEOF());
    }

    @Test
    public void testComment3() throws IOException {
        Tokenizer t = new Tokenizer(new StringReader("<%-- comment--%test --%>"));
        Assert.assertTrue(t.isComment());
        String comment = t.readComment();
        Assert.assertEquals(" comment--%test ", comment);
        Assert.assertTrue(t.isEOF());
    }

    @Test
    public void testComment4() throws IOException {
        Tokenizer t = new Tokenizer(new StringReader("<%-- comment-%>test --%>"));
        Assert.assertTrue(t.isComment());
        String comment = t.readComment();
        Assert.assertEquals(" comment-%>test ", comment);
        Assert.assertTrue(t.isEOF());
    }

    @Test
    public void testOpenTag() throws IOException {
        Tokenizer t = new Tokenizer(new StringReader("<t:test/>"));
        Assert.assertTrue(t.isOpenTag());
        String tag = t.readOpenTag();
        Assert.assertEquals("<t:test", tag);
        Assert.assertTrue(t.isEndOfEmptyTag());
        t.skipEndOfEmptyTag();
        Assert.assertTrue(t.isEOF());

    }
}
