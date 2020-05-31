package com.danielfoord.lox.globals;

public abstract class GlobalFunction implements GlobalCallable {
    @Override
    public String toString() {
        return "<fn natve::" + this.getName() + ">";
    }
}
