/*
  Created By: Fei Song
  File Name: cm.flex
  To Build: jflex cm.flex

  and then after the parser is created
    javac Lexer.java
*/

/* --------------------------Usercode Section------------------------ */

import java_cup.runtime.*;

%%

/* -----------------Options and Declarations Section----------------- */

/*
   The name of the class JFlex will create will be Lexer.
   Will write the code to the file Lexer.java.
*/
%class Lexer

%eofval{
	return null;
%eofval};

/*
  The current line number can be accessed with the variable yyline
  and the current column number with the variable yycolumn.
*/
%line
%column

/*
   Will switch to a CUP compatibility mode to interface with a CUP
   generated parser.
*/
%cup

/*
  Declarations

  Code between %{ and %}, both of which must be at the beginning of a
  line, will be copied letter to letter into the lexer class source.
  Here you declare member variables and functions that are used inside
  scanner actions.
*/
%{
	/* To create a new java_cup.runtime.Symbol with information about
       the current token, the token will have no value in this
       case. */
	private Symbol symbol(int type) {
			return new Symbol(type, yyline, yycolumn);
	}

	/* Also creates a new java_cup.runtime.Symbol with information
       about the current token, but this object has a value. */
	private Symbol symbol(int type, Object value) {
			return new Symbol(type, yyline, yycolumn, value);
	}
%}

/*
  Macro Declarations

  These declarations are regular expressions that will be used latter
  in the Lexical Rules Section.
*/

/* A line terminator is a \r (carriage return), \n (line feed), or \r\n. */
LineTerminator = \r|\n|\r\n

/* White space is a line terminator, space, tab, or form feed. */
WhiteSpace = {LineTerminator}|[ \t\f]

/* A literal integer is is a number beginning with a number between
   one and nine followed by zero or more numbers between zero and nine
   or just a zero.  */
number = [0-9]+

/* A valid identifier can have letters (both uppercase and lowercase letters), 
   digits and underscores. The first letter of an identifier should be either 
   a letter or an underscore. */
identifier = [a-zA-Z_][a-zA-Z0-9_]*

/* Comments are surrounded by the usual C style notations. 
   They can be placed anywhere white space can appear 
   (e.g., comments cannot be placed within tokens) and may include 
   more than one line. Comments may not be nested. */
comment = \/\*([^*]|\s|(\*+([^*/]|\s)))*\*+\/

%%

/* ------------------------Lexical Rules Section---------------------- */

/*
   This section contains regular expressions and actions, i.e. Java
   code, that will be executed when the scanner matches the associated
   regular expression. */

/* C- keywords: all of them are reserved and must be written in lowercase */ 

"int"           { return symbol(sym.INT); }
"void"          { return symbol(sym.VOID); }
"if"            { return symbol(sym.IF); }
"else"					{ return symbol(sym.ELSE); }
"while"					{ return symbol(sym.WHILE); }
"return"				{ return symbol(sym.RETURN); }

/* special symbols */

"+"						  { return symbol(sym.ADD); }
"-"						  { return symbol(sym.SUB); }
"*"						  { return symbol(sym.MUL); }
"="						  { return symbol(sym.ASSN); }
"/"						  { return symbol(sym.DIV); }
"<"						  { return symbol(sym.LT); }
"<="					  { return symbol(sym.LE); }
">"						  { return symbol(sym.GT); }
">="					  { return symbol(sym.GE); }
"=="					  { return symbol(sym.EQ); }
"!="					  { return symbol(sym.NE); }
";"						  { return symbol(sym.SEMI); }
","						  { return symbol(sym.COMMA); }
"("						  { return symbol(sym.LPAREN); }
")"						  { return symbol(sym.RPAREN); }
"["						  { return symbol(sym.LSQUARE); }
"]"						  { return symbol(sym.RSQUARE); }
"{"						  { return symbol(sym.LCURLY); }
"}"					  	{ return symbol(sym.RCURLY); }

/* macro regex */

{number}        { return symbol(sym.NUM, yytext()); }
{identifier}    { return symbol(sym.ID, yytext()); }
{WhiteSpace}    { /* skip whitespace */ }
{comment}       { /* skip comments */ }

. {
    System.err.println("[" + yyline + "] error: undefined reference to \'" + yytext() + "\'");
    return symbol(sym.error);
}