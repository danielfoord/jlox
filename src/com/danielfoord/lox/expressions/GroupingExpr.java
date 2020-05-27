package com.danielfoord.lox.expressions;

public class GroupingExpr extends Expr {
  public final Expr expression;

  public GroupingExpr(Expr expresson) {
    this.expression = expresson;
  }

  @Override
  public <R> R accept(ExprVisitor<R> visitor) {
    return visitor.visitGroupingExpr(this);
  }
}