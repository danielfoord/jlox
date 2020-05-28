package com.danielfoord.lox;

import com.danielfoord.lox.expressions.*;
import com.danielfoord.lox.statements.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// program     → declaration* EOF ;

// declaration → var_declaration | statement ;
// statement   → exprStmt | printStmt | block | ifStmt | whileStmt | forStmt ;

// var_declaration   → "var" IDENTIFIER ( "=" expression )? ";" ;
// block             → "{" declaration* "}" ;
// exprStmt          → expression ";" ;
// printStmt         → "print" expression ";" ;
// ifStmt            → "if" "(" expression ")" statement "else" statement ";" ;
// whileStmt         → "while" "(" expression ")" statement ;
// forStmt           → "for" "(" ( var_declaration | expression  ";" ) expression? ";" expression? ")" statement ;

// expression     → assignment ;
// assignment     → IDENTIFIER "=" assignment | logic_or ;
// logic_or       → logic_and ( "or" logic_and )*;
// logic_and      → equality ( "and" equality )*;
// equality       → comparison ( ( "!=" | "==" ) comparison )* ;
// comparison     → addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
// addition       → multiplication ( ( "-" | "+" ) multiplication )* ;
// multiplication → unary ( ( "/" | "*" ) unary )* ;
// unary          → ( "!" | "-" ) unary | primary ;
// primary        → NUMBER | STRING | "false" | "true" | "nil" | "(" expression ")" | IDENTIFIER;

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
        try {
            if (peekMatch(TokenType.VAR))
                return varDeclaration();
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt statement() {
        if (peekMatch(TokenType.PRINT))
            return printStatement();
        if (peekMatch(TokenType.LEFT_BRACE))
            return new BlockStmt(block());
        if (peekMatch(TokenType.IF))
            return ifStatement();
        if (peekMatch(TokenType.WHILE))
            return whileStatement();
        if (peekMatch(TokenType.FOR))
            return forStatement();

        return expressionStatement();
    }

    private Stmt printStatement() {
        Expr expression = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after expression.");
        return new PrintStmt(expression);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();
        while (!checkNext(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    private Stmt ifStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after condition.");

        Stmt ifStatement = statement();
        Stmt elseStatement = null;
        if (peekMatch(TokenType.ELSE)) {
            elseStatement = statement();
        }
        return new IfStmt(condition, ifStatement, elseStatement);
    }

    private Stmt whileStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'");
        Expr expression = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after condition");
        Stmt statement = statement();
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
        Stmt body = statement();

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

    private Stmt varDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (peekMatch(TokenType.EQUAL)) {
            initializer = expression();
        }

        consume(TokenType.SEMICOLON, "Expect ';' after expression.");
        return new VarStmt(name, initializer);
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

        return primary();
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
            if (previous().type == TokenType.SEMICOLON)
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

    private static class ParseError extends RuntimeException {
        private static final long serialVersionUID = 4603695572380937534L;
    }
    //#endregion
}