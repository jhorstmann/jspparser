package net.jhorstmann.jspparser;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.StringReader;
import junit.framework.Assert;
import org.junit.Test;

public class LookAheadReaderTest {

    @Test
    public void testLa1() throws IOException {
        LookAheadReader lr = new LookAheadReader(new StringReader("abc"), 1);
        Assert.assertEquals('a', lr.la1());
        Assert.assertEquals('a', lr.la1());
        Assert.assertEquals('a', lr.la1());
        int a = lr.read();
        Assert.assertEquals('a', a);

        Assert.assertEquals('b', lr.la1());
        Assert.assertEquals('b', lr.la1());
        int b = lr.read();
        Assert.assertEquals('b', b);

        Assert.assertEquals('c', lr.la1());
        Assert.assertEquals('c', lr.la1());
        Assert.assertEquals('c', lr.la1());
        int c = lr.read();
        Assert.assertEquals('c', c);
        int e = lr.read();
        Assert.assertEquals(-1, e);

    }

    @Test
    public void testLookingAt() throws IOException {
        LookAheadReader lr = new LookAheadReader(new StringReader("abcdefghij"), 4);
        Assert.assertTrue(lr.lookingAt("ab"));
        Assert.assertTrue(lr.lookingAt("abc"));
        Assert.assertTrue(lr.lookingAt("abcd"));
        Assert.assertTrue(lr.lookingAt("abcd", true));

        Assert.assertFalse(lr.lookingAt("abcd"));
        Assert.assertFalse(lr.lookingAt("efx"));
        Assert.assertFalse(lr.lookingAt("efxyz"));
        Assert.assertTrue(lr.lookingAt("efg"));
        Assert.assertTrue(lr.lookingAt("efg", true));
        Assert.assertFalse(lr.lookingAt("xyz"));
        Assert.assertFalse(lr.lookingAt("hijxyz"));

        Assert.assertTrue(lr.lookingAt("hij", true));
        Assert.assertEquals(-1, lr.la1());
    }

}
