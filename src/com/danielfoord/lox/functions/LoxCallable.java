package com.danielfoord.lox.functions;

import com.danielfoord.lox.Interpreter;

import java.io.IOException;
import java.util.List;

public interface LoxCallable {
    int arity();

    Object call(Interpreter interpreter, List<Object> arguments) throws Exception;
}