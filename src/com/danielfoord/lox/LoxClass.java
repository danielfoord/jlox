package com.danielfoord.lox;

import com.danielfoord.lox.functions.LoxCallable;
import com.danielfoord.lox.functions.LoxFunction;

import java.util.List;
import java.util.Map;

public class LoxClass implements LoxCallable {
    public final String name;
    public final Map<String, LoxFunction> methods;

    public LoxClass(String name, Map<String, LoxFunction> methods) {
        this.name = name;
        this.methods = methods;
    }

    @Override
    public int arity() {
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        return new LoxInstance(this);
    }

    public LoxFunction findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }
        return null;
    }

    @Override
    public String toString() {
        return name;
    }
}
