package com.danielfoord.lox.globals;

import com.danielfoord.lox.Interpreter;

import java.util.List;

public class Clock extends GlobalFunction {
    @Override
    public int arity() {
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) throws Exception {
        return (double) System.currentTimeMillis() / 1000.0;
    }

    @Override
    public String getName() {
        return "clock";
    }
}
