package com.danielfoord.lox;

import java.io.Serializable;

public class Token implements Serializable {
    public final TokenType type;
    public final String lexeme;
    public final Object literal;
    public final int line;

    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}