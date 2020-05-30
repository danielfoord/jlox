package com.danielfoord.lox.functions;

public class Return extends RuntimeException {
    public final Object value;

    public Return (Object value) {
        super();
        this.value = value;
    }
}
