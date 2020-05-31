package com.danielfoord.lox.functions;

import com.danielfoord.lox.Environment;
import com.danielfoord.lox.Interpreter;
import com.danielfoord.lox.LoxInstance;
import com.danielfoord.lox.statements.FunctionStmt;

import java.util.List;

public class LoxFunction implements LoxCallable {

    private final FunctionStmt declaration;
    private final Environment closure;
    private final boolean isInitializer;

    public LoxFunction(FunctionStmt declaration, Environment closure, boolean isInitializer) {
        this.declaration = declaration;
        this.closure = closure;
        this.isInitializer = isInitializer;
    }

    @Override
    public int arity() {
        return declaration.parameters.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);
        for (var i = 0; i < arity(); i++) {
            environment.define(declaration.parameters.get(i).lexeme, arguments.get(i));
        }
        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {
            if (isInitializer) return closure.getAt(0, "this");
            return returnValue.value;
        }

        if (isInitializer) {
            return this.closure.getAt(0, "this");
        }
        return null;
    }

    public LoxFunction bind(LoxInstance instance) {
        Environment env = new Environment(closure);
        env.define("this", instance);
        return new LoxFunction(declaration, env, isInitializer);
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }
}
