package com.danielfoord.lox.globals;

import com.danielfoord.lox.functions.LoxCallable;

public interface GlobalCallable extends LoxCallable {
    String getName();
}
