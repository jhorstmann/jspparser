package net.jhorstmann.jspparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class LookAheadReader extends Reader {

    private Reader reader;
    private int lookahead;

    public LookAheadReader(Reader reader, int lookahead) {
        this.reader = reader instanceof BufferedReader ? reader : new BufferedReader(reader, Math.max(lookahead, 4096));
        this.lookahead = lookahead;
    }

    public void mark() throws IOException {
        reader.mark(lookahead);
    }

    @Override
    public void reset() throws IOException {
        reader.reset();
    }

    public int la1() throws IOException {
        synchronized (lock) {
            reader.mark(lookahead);
            int ch = reader.read();
            reader.reset();
            return ch;
        }
    }

    public int la2() throws IOException {
        synchronized (lock) {
            reader.mark(lookahead);
            int ch = reader.read();
            if (ch != -1) {
                ch = reader.read();
            }
            reader.reset();
            return ch;
        }
    }

    public int la3() throws IOException {
        synchronized (lock) {
            reader.mark(lookahead);
            int ch = reader.read();
            if (ch != -1) {
                ch = reader.read();
                if (ch != -1) {
                    ch = reader.read();
                }
            }
            reader.reset();
            return ch;
        }
    }

    public boolean lookingAt(String token, boolean consume) throws IOException {
        synchronized (lock) {
            boolean res = true;
            reader.mark(lookahead);
            for (int i = 0, len = token.length(); i < len; i++) {
                int ch = reader.read();
                if (ch != token.charAt(i)) {
                    res = false;
                    break;
                }
            }
            if (!consume || !res) {
                reader.reset();
            }
            return res;
        }
    }
    
    public boolean lookingAt(String token) throws IOException {
        return lookingAt(token, false);
    }

    public void consume(String token) throws IOException {
        synchronized (lock) {
            if (!lookingAt(token, true)) {
                throw new SyntaxException("String to consume does not match");
            }
        }
    }

    public void consume(int token) throws IOException {
        int ch = read();
        if (ch != token) {
            throw new SyntaxException("String to consume does not match");
        }
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        return reader.read(cbuf, off, len);
    }

    @Override
    public int read(char[] cbuf) throws IOException {
        return read(cbuf, 0, cbuf.length);
    }

    @Override
    public int read() throws IOException {
        return reader.read();
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
