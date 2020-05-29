# Lox 

An implementation of Lox from [craftinginterpreters.com](craftinginterpreters.com).
With some of the challenges implemented.

```
program           → declaration* EOF ;

declaration       → var_declaration | statement ;
loopDeclaration   → var_declaration | loopStatement ;

var_declaration   → "var" IDENTIFIER ( "=" expression )? ";" ;

statement         → exprStmt | printStmt | block | ifStmt | whileStmt | forStmt ;
loopStatement     → exprStmt | printStmt | loopBlock | loopIfStmt | whileStmt | forStmt | break;

printStmt         → "print" expression ";" ;
break             → "break" ";" ;
block             → "{" declaration* "}" ;
loopBlock         → "{" ( loopDeclaration* ) break? "}" ;
exprStmt          → expression ";" ;
ifStmt            → "if" "(" expression ")" statement "else" statement ";" ;
loopIfStmt        → "if" "(" expression ")" loopStatement "else" loopStatement ";" ;
whileStmt         → "while" "(" expression ")" loopStatement ;
forStmt           → "for" "(" ( var_declaration | expression  ";" ) expression? ";" expression? ")" loopStatement ;

expression        → assignment ;
assignment        → IDENTIFIER "=" assignment | logic_or ;
logic_or          → logic_and ( "or" logic_and )*;
logic_and         → equality ( "and" equality )*;
equality          → comparison ( ( "!=" | "==" ) comparison )* ;
comparison        → addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
addition          → multiplication ( ( "-" | "+" ) multiplication )* ;
multiplication    → unary ( ( "/" | "*" ) unary )* ;
unary             → ( "!" | "-" ) unary | primary ;
primary           → NUMBER | STRING | "false" | "true" | "nil" | "(" expression ")" | IDENTIFIER;
```