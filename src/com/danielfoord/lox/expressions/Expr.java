package com.danielfoord.lox.expressions;

// Expressions

// expression → literal
//            | unary
//            | binary
//            | grouping ;

// literal    → NUMBER | STRING | "true" | "false" | "nil" ;
// grouping   → "(" expression ")" ;
// unary      → ( "-" | "!" ) expression ;
// binary     → expression operator expression ;
// operator   → "==" | "!=" | "<" | "<=" | ">" | ">="
//            | "+"  | "-"  | "*" | "/" ;

import java.io.Serializable;

public abstract class Expr implements Serializable {
    public abstract <R> R accept(ExprVisitor<R> visitor);
}