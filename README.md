# Lox 

An implementation of Lox from [craftinginterpreters.com](https://craftinginterpreters.com).
With some challenges implemented.

<hr >

#### Context free grammar:

##### Syntactic Grammar
```
program           → declaration* EOF ;

declaration       → varDeclaration | statement | funDecl | classDecl;
loopDeclaration   → varDeclaration | loopStatement ;

varDeclaration    → "var" IDENTIFIER ( "=" expression )? ";" ;

statement         → exprStmt | printStmt | block | ifStmt | whileStmt | forStmt | returnStmt;
loopStatement     → exprStmt | printStmt | loopBlock | loopIfStmt | whileStmt | forStmt | breakStmt | returnStmt;
printStmt         → "print" expression ";" ;

classDecl         → "class" IDENTIFIER ("<" IDENTIFIER)? "{" function* "}" ;

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

expression        → assignment ;
assignment        → ( call ".")? IDENTIFIER "=" assignment | logic_or ;
logic_or          → logic_and ( "or" logic_and )*;
logic_and         → equality ( "and" equality )*;
equality          → comparison ( ( "!=" | "==" ) comparison )* ;
comparison        → addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
addition          → multiplication ( ( "-" | "+" ) multiplication )* ;
multiplication    → unary ( ( "/" | "*" ) unary )* ;
unary             → ( "!" | "-" ) unary | call ;    
call              → primary ( ( "(" arguments? ")" | "." IDENTIFIER ) )* ;
arguments         → expression ( "," expression )* ;
primary           → NUMBER | STRING | "false" | "true" | "nil" | "(" expression ")" | IDENTIFIER | "super" . IDENTIFER ;
```
<hr >

#### Yet to be implemented:

 - [ ] Try/Catch statement
 - [ ] `assert` built in method
 - [ ] Runtime jar output
 - [ ] Runtime docker image publish
 - [ ] Lox native methods for writing simple unit tests
 - [ ] Namespacing/Packaging
 - [ ] Vscode plugin
