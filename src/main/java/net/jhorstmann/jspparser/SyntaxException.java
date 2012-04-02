package net.jhorstmann.jspparser;

import java.io.IOException;

public class SyntaxException extends IOException {

    public SyntaxException(String message) {
        super(message);
    }

    public SyntaxException() {
    }

}
