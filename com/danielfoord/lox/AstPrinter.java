package com.danielfoord.lox;

import com.danielfoord.lox.expressions.AssignExpr;
import com.danielfoord.lox.expressions.BinaryExpr;
import com.danielfoord.lox.expressions.Expr;
import com.danielfoord.lox.expressions.ExprVisitor;
import com.danielfoord.lox.expressions.GroupingExpr;
import com.danielfoord.lox.expressions.LiteralExpr;
import com.danielfoord.lox.expressions.UnaryExpr;
import com.danielfoord.lox.expressions.VariableExpr;

public class AstPrinter implements ExprVisitor<String> {

  @Override
  public String visitBinaryExpr(BinaryExpr expression) {
    return parenthesize(expression.operator.lexeme, expression.left, expression.right);
  }

  @Override
  public String visitLiteralExpr(LiteralExpr expression) {
    if (expression.value == null) {
      return "nil";
    }
    return expression.value.toString();
  }

  @Override
  public String visitGroupingExpr(GroupingExpr expression) {
    return parenthesize("group", expression.expression);
  }

  @Override
  public String visitUnaryExpr(UnaryExpr expression) {
    return parenthesize(expression.operator.lexeme, expression.right);
  }

  @Override
  public String visitVariableExpr(VariableExpr expression) {
    return parenthesize(expression.name.lexeme, expression);
  }

  public String print(Expr expr) {
    return expr.accept(this);
  }

  private String parenthesize(String name, Expr... exprs) {
    StringBuilder builder = new StringBuilder();

    builder.append("(").append(name);
    for (Expr expr : exprs) {
      builder.append(" ");
      builder.append(expr.accept(this));
    }
    builder.append(")");

    return builder.toString();
  }

  @Override
  public String visitAssignExpr(AssignExpr expression) {
    return parenthesize(expression.name.lexeme, expression);
  }
}