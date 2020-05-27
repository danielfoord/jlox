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

    public Object get(Token identifer) {
        if (values.containsKey(identifer.lexeme)) {
            return values.get(identifer.lexeme);
        }

        if (enclosing != null) {
            return enclosing.get(identifer);
        }

        throw new RuntimeError(identifer, "Undefined variable '" + identifer.lexeme + "'.");
    }

    public Object assign(Token identifer, Object value) {
        if (values.containsKey(identifer.lexeme)) {
            values.put(identifer.lexeme, value);
            return value;
        }

        if (enclosing != null) {
            return enclosing.assign(identifer, value);
        }

        throw new RuntimeError(identifer, "Undefined variable '" + identifer.lexeme + "'.");
    }
}