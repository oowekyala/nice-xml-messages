package com.github.oowekyala.ooxml.xpath;

import static com.github.oowekyala.ooxml.xpath.TokenKinds.*;

%%
%class      XPathLexer
%type	    TokenKinds
%unicode
%buffer     200 // reduce buffer size
%eofval{
EOF;
%eofval}
%{
%}


// keywords & such

node_kind = "node" | "document-node" | "text" | "comment" /*| "namespace-node"*/ | "processing-instruction" /*| "schema-attribute"*/ | "element" /*| "schema-element" | "function"*/
axis      = "self" | "child" | "attribute" | "descendant" | "descendant-or-self" | "ancestor" | "ancestor-or-self" /*| "following"*/ | "following-sibling" /*| "namespace"*/ | "parent" /*| "preceding"*/ | "preceding-sibling"
str_op    = "or" | "and" /*| "div" | "idiv" | "mod" | "union" | "intersect" | "except" | "ne" | "eq" | "lt" | "le" | "gt" | "ge"*/
punct_op  = "=" | "!=" | "<" | "<=" | ">" | ">="

// literals

string_literal = \" (\"\" | ~[\"])* \" | ' ('' | ~['])* '

integerLiteral = {digits}
decimalLiteral = "." {digits} | {digits} "." {maybe_digits}
doubleLiteral  = ("." {digits} | {digits} ("." {maybe_digits})?) {exponent}

digits       = [0-9]+
maybe_digits = [0-9]*
exponent     = [Ee] [+-]? {digits}

// names
ncname = {name_start_char_no_colon} {name_char_no_colon}*

name_start_char_no_colon = 
    [A-Za-z_\u00c0-\u00d6\u00d8-\u00f6\u00f8-\u02ff\u0370-\u037d\u037f-\u1fff\u200c-\u200d\u2070-\u218f\u2c00-\u2fef\u3001-\ud7ff\uf900-\ufdcf\ufdf0-\ufffd\u1000-\uefff]

name_char_no_colon = {name_start_char_no_colon} | [-.0-9\u00b7\u0300-\u036f\u203f-\u2040] 

// whitespace

white_space = [\R \t\f]+

%%

"//"                   { return PATH;        }
"/"                    { return PATH_DOUBLE; }

"["                    { return LBRACKET;    }
"]"                    { return RBRACKET;    }
"::"                   { return DOUBLE_COLON;}
"@"                    { return AT;          }
"."                    { return PERIOD;      }
"*"                    { return STAR;        }
"$"                    { return DOLLAR;      }

{str_op} | {punct_op}  { return OP;          }

{string_literal}       { return LIT_STRING;  }
{doubleLiteral}        { return LIT_DOUBLE;  }
{decimalLiteral}       { return LIT_DECIMAL; }
{integerLiteral}       { return LIT_INTEGER; }

{axis}                 { return AXIS;        }
{node_kind}            { return NODE_KIND;   }

{ncname}               { return IDENTIFIER;  }

{white_space}          { return IGNORED;     }

[^]                    { throw new IllegalArgumentException("Illegal character <" + yytext() + ">"); }
