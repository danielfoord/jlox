package com.danielfoord.lox.globals;

import com.danielfoord.lox.Interpreter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

public class ReadLine extends GlobalFunction {
    @Override
    public int arity() {
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) throws Exception {
        var buffer = new BufferedReader(new InputStreamReader(System.in));
        return buffer.readLine();
    }

    @Override
    public String getName() {
        return "readLine";
    }
}
