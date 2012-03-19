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

}
