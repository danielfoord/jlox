package com.danielfoord.lox;

import com.danielfoord.lox.functions.LoxCallable;
import com.danielfoord.lox.functions.LoxFunction;

import java.util.List;
import java.util.Map;

public class LoxClass implements LoxCallable {
    
    public final String name;
    public final LoxClass superClass;
    public final Map<String, LoxFunction> methods;

    public LoxClass(String name, LoxClass superClass, Map<String, LoxFunction> methods) {
        this.name = name;
        this.superClass = superClass;
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

        if (superClass != null) {
            return superClass.findMethod(name);
        }

        return null;
    }

    @Override
    public String toString() {
        return name;
    }
}
