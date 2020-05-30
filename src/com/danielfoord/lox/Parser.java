package com.danielfoord.lox;

import com.danielfoord.lox.expressions.*;
import com.danielfoord.lox.statements.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }
        return statements;
    }

    //#region Statements
    private Stmt declaration() {
        return declaration(false);
    }

    private Stmt declaration(boolean loopStatement) {
        try {
            if (peekMatch(TokenType.VAR))
                return varDeclaration();
            if (peekMatch(TokenType.FUN))
                return function("function");
            return statement(loopStatement);
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt varDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (peekMatch(TokenType.EQUAL)) {
            initializer = expression();
        }

        consume(TokenType.SEMICOLON, "Expect ';' after expression.");
        return new VarStmt(name, initializer);
    }

    private Stmt function(String kind) {
        Token name = consume(TokenType.IDENTIFIER, "Expect " + kind + " name.");
        consume(TokenType.LEFT_PAREN, "Expect '(' after " + kind + " name.");
        List<Token> parameters = new ArrayList<>();
        if (!checkNext(TokenType.RIGHT_PAREN)) {
            do {
                if (parameters.size() >= 255) {
                    error(peek(), "Cannot have more than 255 parameters.");
                }

                parameters.add(consume(TokenType.IDENTIFIER, "Expect parameter name."));
            } while (peekMatch(TokenType.COMMA));
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters.");
        consume(TokenType.LEFT_BRACE, "Expect '{' before " + kind + " body.");
        List<Stmt> body = block(false);
        return new FunctionStmt(name, parameters, body);
    }

    private Stmt statement(boolean loopStatement) {
        if (peekMatch(TokenType.PRINT))
            return printStatement();
        if (peekMatch(TokenType.LEFT_BRACE))
            return new BlockStmt(block(loopStatement));
        if (peekMatch(TokenType.IF))
            return ifStatement(loopStatement);
        if (peekMatch(TokenType.WHILE))
            return whileStatement();
        if (peekMatch(TokenType.FOR))
            return forStatement();
        if (peekMatch(TokenType.RETURN))
            return returnStatement();
        if (loopStatement && peekMatch(TokenType.BREAK))
            return breakStatement();

        return expressionStatement();
    }

    private Stmt breakStatement() {
        consume(TokenType.SEMICOLON, "Expect ';' after 'break'.");
        return new BreakStmt();
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!checkNext(TokenType.SEMICOLON)) {
            value = expression();
        }

        consume(TokenType.SEMICOLON, "Expect ';' after return value.");
        return new ReturnStmt(keyword, value);
    }

    private Stmt printStatement() {
        Expr expression = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after expression.");
        return new PrintStmt(expression);
    }

    private List<Stmt> block(boolean loopStatement) {
      try {
          List<Stmt> statements = new ArrayList<>();
          while (!checkNext(TokenType.RIGHT_BRACE) && !isAtEnd()) {
              Stmt statement = declaration(loopStatement);
              statements.add(statement);
              if (statement instanceof BreakStmt && (!checkNext(TokenType.RIGHT_BRACE) && !isAtEnd())) {
                  throw error (peek(), "Unreachable code");
              }
          }
          consume(TokenType.RIGHT_BRACE, "Expect '}' after block.");
          return statements;
      } catch(ParseError error) {
          synchronize();
          return null;
      }
    }

    private Stmt ifStatement(boolean loopStatement) {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after condition.");

        Stmt ifStatement = statement(loopStatement);
        Stmt elseStatement = null;
        if (peekMatch(TokenType.ELSE)) {
            elseStatement = statement(loopStatement);
        }
        return new IfStmt(condition, ifStatement, elseStatement);
    }

    private Stmt whileStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'");
        Expr expression = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after condition");
        Stmt statement = statement(true);
        return new WhileStmt(expression, statement);
    }

    private Stmt forStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'for'");

        Stmt initializer;
        if (peekMatch(TokenType.SEMICOLON)) {
            initializer = null;
        } else if (peekMatch((TokenType.VAR))) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        Expr condition = null;
        if (!checkNext(TokenType.SEMICOLON)) {
            condition = expression();
        }
        consume(TokenType.SEMICOLON, "Expect ';' after loop condition");

        Expr increment = null;
        if (!checkNext(TokenType.RIGHT_PAREN)) {
            increment = expression();
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after for clauses");
        Stmt body = statement(true);

        if (increment != null) {
            body = new BlockStmt(Arrays.asList(body, new ExpressionStmt(increment)));
        }

        if (condition == null) {
            condition = new LiteralExpr(true);
        }

        body = new WhileStmt(condition, body);

        if (initializer != null) {
            body = new BlockStmt(Arrays.asList(initializer, body));
        }

        return body;
    }

    private Stmt expressionStatement() {
        Expr expression = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after expression.");
        return new ExpressionStmt(expression);
    }
    //#endregion

    //#region Expressions
    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = logicOr();

        if (peekMatch(TokenType.EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof VariableExpr) {
                Token name = ((VariableExpr) expr).name;
                return new AssignExpr(name, value);
            }

            throw error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr logicOr() {
        Expr expression = logicAnd();

        while (peekMatch(TokenType.OR)) {
            Token operator = previous();
            Expr rightExpression = logicAnd();
            expression = new LogicExpr(expression, operator, rightExpression);
        }

        return expression;
    }

    private Expr logicAnd() {
        Expr expression = equality();

        while (peekMatch(TokenType.AND)) {
            Token operator = previous();
            Expr rightExpression = equality();
            expression = new LogicExpr(expression, operator, rightExpression);
        }

        return expression;
    }

    private Expr equality() {
        Expr expr = comparison();

        while (peekMatch(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new BinaryExpr(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = addition();

        while (peekMatch(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Expr right = addition();
            expr = new BinaryExpr(expr, operator, right);
        }

        return expr;
    }

    private Expr addition() {
        Expr expr = multiplication();

        while (peekMatch(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = multiplication();
            expr = new BinaryExpr(expr, operator, right);
        }

        return expr;
    }

    private Expr multiplication() {
        Expr expr = unary();

        while (peekMatch(TokenType.STAR, TokenType.SLASH)) {
            Token operator = previous();
            Expr right = unary();
            expr = new BinaryExpr(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (peekMatch(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new UnaryExpr(operator, right);
        }

        return call();
    }

    private Expr call() {
        Expr expression = primary();

        while (true) {
            if (peekMatch(TokenType.LEFT_PAREN)) {
                expression = finishCall(expression);
            } else {
                break;
            }
        }
        return expression;
    }

    private Expr primary() {
        if (peekMatch(TokenType.FALSE))
            return new LiteralExpr(false);
        if (peekMatch(TokenType.TRUE))
            return new LiteralExpr(true);
        if (peekMatch(TokenType.NIL))
            return new LiteralExpr(null);
        if (peekMatch(TokenType.IDENTIFIER))
            return new VariableExpr(previous());
        if (peekMatch(TokenType.NUMBER, TokenType.STRING))
            return new LiteralExpr(previous().literal);

        if (peekMatch(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return new GroupingExpr(expr);
        }

        throw error(peek(), "Expect expression.");
    }
    //#endregion

    //#region Util
    private Token consume(TokenType type, String message) {
        if (checkNext(type))
            return advance();

        throw error(peek(), message);
    }

    private Token advance() {
        if (!isAtEnd()) {
            current++;
        }
        return previous();
    }

    private boolean peekMatch(TokenType... types) {
        for (TokenType type : types) {
            if (checkNext(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean checkNext(TokenType type) {
        if (isAtEnd()) {
            return false;
        }
        return peek().type == type;
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            // If we are at the end of a statement, but we want to advance if we are at the end of a block
            if (previous().type == TokenType.SEMICOLON && peek().type != TokenType.RIGHT_BRACE)
                return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
                default:
                    break;
            }

            advance();
        }
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!checkNext(TokenType.RIGHT_PAREN)) {
            do {
                if (arguments.size() >= 255) { // Same as Java
                    error(peek(), "Cannot have more than 255 arguments.");
                }
                arguments.add(expression());
            } while (peekMatch(TokenType.COMMA));
        }

        Token paren = consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments.");

        return new CallExpr(callee, paren, arguments);
    }

    private static class ParseError extends RuntimeException {
        private static final long serialVersionUID = 4603695572380937534L;
    }
    //#endregion
}