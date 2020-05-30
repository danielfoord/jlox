package com.danielfoord.lox.functions;

import com.danielfoord.lox.Environment;
import com.danielfoord.lox.Interpreter;
import com.danielfoord.lox.statements.FunctionStmt;

import java.util.List;

public class LoxFunction implements LoxCallable {

    private final FunctionStmt declaration;

    public LoxFunction(FunctionStmt declaration) {
        this.declaration = declaration;
    }

    @Override
    public int arity() {
        return declaration.parameters.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(interpreter.environment);
        for (var i = 0; i < arity(); i++) {
            environment.define(declaration.parameters.get(i).lexeme, arguments.get(i));
        }
        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {
            return returnValue.value;
        }
        return null;
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }
}
