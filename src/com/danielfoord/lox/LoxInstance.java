package com.danielfoord.lox;

import com.danielfoord.lox.functions.LoxFunction;

import java.util.HashMap;
import java.util.Map;

public class LoxInstance {

    private final LoxClass klass;
    private final Map<String, Object> fields = new HashMap<>();

    public LoxInstance(LoxClass klass) {
        this.klass = klass;
    }

    public void set(Token name, Object value) {
        fields.put(name.lexeme, value);
    }

    public Object get(Token name) {
        if (fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme);
        }

        LoxFunction method = klass.findMethod(name.lexeme);
        if (method != null) return method.bind(this);

        throw new RuntimeError(name, "Undefined property '" + name.lexeme + "' on " + this.toString() + ".");
    }

    @Override
    public String toString() {
        return klass.name + " instance";
    }
}
