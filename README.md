# JLite compiler

Compiler for a simple Java-like language. Some sample JLite programs are in `testcases/`

The compiler operates in 4 phrases:
* Parsing: the input is parsed with a CUP generated parser to an AST
* Typechecking: the AST is type-checked and annotated
* Intermediate code generation: an IR3 code is generated from the checked AST
* Native code generation: the ARM backend generates native code from IR3

