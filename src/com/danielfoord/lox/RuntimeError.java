package com.danielfoord.lox;

public class RuntimeError extends RuntimeException {
    private static final long serialVersionUID = 1573765504196826664L;
    final Token token;

    public RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }
}