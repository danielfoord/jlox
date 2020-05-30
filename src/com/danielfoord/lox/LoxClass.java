package com.danielfoord.lox;

import com.danielfoord.lox.functions.LoxCallable;

import java.util.List;

public class LoxClass implements LoxCallable {
    public final String name;

    public LoxClass(String name) {
        this.name = name;
    }

    @Override
    public int arity() {
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        return new LoxInstance(this);
    }

    @Override
    public String toString() {
        return name;
    }
}
