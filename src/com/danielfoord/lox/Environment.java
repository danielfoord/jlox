package com.danielfoord.lox;

import java.util.HashMap;

public class Environment {

    public final Environment enclosing;
    private final HashMap<String, Object> values = new HashMap<>();

    public Environment() {
        this.enclosing = null;
    }

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    public void define(String name, Object value) {
        values.put(name, value);
    }

    public Object get(Token identifier) {
        if (values.containsKey(identifier.lexeme)) {
            return values.get(identifier.lexeme);
        }

        if (enclosing != null) {
            return enclosing.get(identifier);
        }

        throw new RuntimeError(identifier, "Undefined variable '" + identifier.lexeme + "'.");
    }

    public Object assign(Token identifier, Object value) {
        if (values.containsKey(identifier.lexeme)) {
            values.put(identifier.lexeme, value);
            return value;
        }

        if (enclosing != null) {
            return enclosing.assign(identifier, value);
        }

        throw new RuntimeError(identifier, "Undefined variable '" + identifier.lexeme + "'.");
    }
}