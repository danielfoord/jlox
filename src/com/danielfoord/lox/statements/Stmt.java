package com.danielfoord.lox.statements;

public abstract class Stmt {
    public abstract <R> R accept(StmtVisitor<R> visitor);
}