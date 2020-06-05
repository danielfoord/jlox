package com.danielfoord.lox.statements;

import java.io.Serializable;

public abstract class Stmt implements Serializable {
    public abstract <R> R accept(StmtVisitor<R> visitor);
}