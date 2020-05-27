package com.danielfoord.lox.expressions;

public class LiteralExpr extends Expr {
  public final Object value;

  public LiteralExpr(Object value) {
    this.value = value;
  }

  @Override
  public <R> R accept(ExprVisitor<R> visitor) {
    return visitor.visitLiteralExpr(this);
  }
}