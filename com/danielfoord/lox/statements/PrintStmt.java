package com.danielfoord.lox.statements;

import com.danielfoord.lox.expressions.Expr;

public class PrintStmt extends Stmt {

    public final Expr expression;

    public PrintStmt(Expr expression) {
        this.expression = expression;
    }
    
	@Override
	public <R> R accept(StmtVisitor<R> visitor) {
        return visitor.visitPrintStmt(this);
	}
    
}