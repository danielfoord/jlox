# Lox 

An implementation of Lox from [craftinginterpreters.com](craftinginterpreters.com).
With some challenges implemented.

#### Context free grammar:

##### Syntactic Grammar
```
program           → declaration* EOF ;

declaration       → varDeclaration | statement | funDecl ;
loopDeclaration   → varDeclaration | loopStatement ;

varDeclaration    → "var" IDENTIFIER ( "=" expression )? ";" ;

statement         → exprStmt | printStmt | block | ifStmt | whileStmt | forStmt | returnStmt;
loopStatement     → exprStmt | printStmt | loopBlock | loopIfStmt | whileStmt | forStmt | breakStmt | returnStmt;
printStmt         → "print" expression ";" ;

funDecl           → "fun" function;
function          → IDENTIFIER "(" parameters? ")" block
parameters        → IDENTIFIER ( "," IDENTIFIER )* ;

breakStmt         → "break" ";" ;
returnStmt        → "return" expression? ";" ;
block             → "{" declaration* "}" ;
functionBlock     → "{" ( declaration* ) returnStmt? "}" ;
loopBlock         → "{" ( loopDeclaration* ) breakStmt? "}" ;
exprStmt          → expression ";" ;
ifStmt            → "if" "(" expression ")" statement "else" statement ";" ;
loopIfStmt        → "if" "(" expression ")" loopStatement "else" loopStatement ";" ;
whileStmt         → "while" "(" expression ")" loopStatement ;
forStmt           → "for" "(" ( varDeclaration | expression  ";" ) expression? ";" expression? ")" loopStatement ;
```
##### Lexical Grammar
```
arguments         → expression ( "," expression )* ;
expression        → assignment ;
assignment        → IDENTIFIER "=" assignment | logic_or ;
logic_or          → logic_and ( "or" logic_and )*;
logic_and         → equality ( "and" equality )*;
equality          → comparison ( ( "!=" | "==" ) comparison )* ;
comparison        → addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
addition          → multiplication ( ( "-" | "+" ) multiplication )* ;
multiplication    → unary ( ( "/" | "*" ) unary )* ;
unary             → ( "!" | "-" ) unary | call ;
call              → primary ( "(" arguments? ")" )* ;
primary           → NUMBER | STRING | "false" | "true" | "nil" | "(" expression ")" | IDENTIFIER;
```