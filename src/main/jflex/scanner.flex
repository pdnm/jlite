package parser.gen;

import java_cup.runtime.*;
import ast.*;

%%

%class Scanner
%implements sym
%unicode
%cup
%line
%column

%{
  StringBuffer string = new StringBuffer();

  private Symbol symbol(int type) {
    return new Symbol(type, yyline, yycolumn);
  }
  private Symbol symbol(int type, Object value) {
    return new Symbol(type, yyline, yycolumn, value);
  }
%}

LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]
WhiteSpace     = {LineTerminator} | [ \t\f]

/* comments */
Comment = {BlockComment} | {InlineComment}

BlockComment   = "/*" ~"*/"
InlineComment     = "//" {InputCharacter}* {LineTerminator}?

Identifier = [:lowercase:] ([:jletterdigit:])*
TypeName   = [:uppercase:] ([:jletterdigit:])*

IntegerLiteral = 0 | [1-9][0-9]*
StringLiteral = 0 | [1-9][0-9]*

%state STRING

%%

<YYINITIAL> {
/* keywords */
"if"           { return symbol(sym.IF); }
"else"            { return symbol(sym.ELSE); }
"while"              { return symbol(sym.WHILE); }
"return"              { return symbol(sym.RETURN); }
"class"              { return symbol(sym.CLASS); }
"new"              { return symbol(sym.NEW); }
"this"              { return symbol(sym.THIS); }
"null"              { return symbol(sym.NULL); }

/* separators */
";"                            { return symbol(SEMI); }
","                            { return symbol(COMMA); }
"."                            { return symbol(DOT); }
"("                            { return symbol(LPAR); }
")"                            { return symbol(RPAR); }
"{"                            { return symbol(LBRACE); }
"}"                            { return symbol(RBRACE); }

/* operators */
"!"                            { return symbol(NEG); }
"+"                            { return symbol(PLUS); }
"-"                            { return symbol(MINUS); }
"*"                            { return symbol(TIMES); }
"/"                            { return symbol(DIVIDE); }
">"                            { return symbol(GT); }
"<"                            { return symbol(LT); }
">="                           { return symbol(GE); }
"<="                           { return symbol(LE); }
"="                            { return symbol(ASSIGN); }
"=="                           { return symbol(EQ); }
"!="                           { return symbol(NEQ); }
"||"                           { return symbol(OR); }
"&&"                           { return symbol(AND); }

  /* identifiers */
  {Identifier}                   { return symbol(sym.ID, new Identifier(yytext())); }
  {TypeName}                     { return symbol(sym.TYPE, new Type(yytext())); }

  /* literals */
  {IntegerLiteral}            { return symbol(sym.INTEGER, Integer.parseInt(yytext())); }
  \"                             { string.setLength(0); yybegin(STRING); }

  "true"                         { return symbol(BOOLEAN, true); }
  "false"                        { return symbol(BOOLEAN, false); }

  /* operators */
  "="                            { return symbol(sym.EQ); }
  "=="                           { return symbol(sym.EQEQ); }
  "+"                            { return symbol(sym.PLUS); }

  /* comments */
  {Comment}                      { /* ignore */ }

  /* whitespace */
  {WhiteSpace}                   { /* ignore */ }
}

<STRING> {
  \"                             { yybegin(YYINITIAL); return symbol(sym.STRING, string.toString()); }
  [^\n\r\"\\]+                   { string.append( yytext() ); }
  \\n                            { string.append('\n'); }
  \\r                            { string.append('\r'); }
  \\t                            { string.append('\t'); }
  \\b                            { string.append('\b'); }
  \\\"                           { string.append('\"'); }
  \\                             { string.append('\\'); }
}

/* error fallback */
[^]                              { throw new Error("Illegal character <"+
                                                    yytext()+">"); }