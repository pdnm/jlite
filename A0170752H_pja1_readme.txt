CS4212 Programming Assignment 1
Author: Phan Duc Nhat Minh

Note: The project requires Java 11 or higher.

Content:
Project Structure:
    - /src/main/ast: Classes for AST nodes
    - /src/main/cup/parser.cup
    - /src/main/jflex/scanner.flex
    - /src/main/parser/gen: Generated code from Cup & JFlex
    - /src/main/parser/ParserDriver.java: Combine the generated lexer and parser to parse input source to a Program representation
    - /src/main/processor: Contains pretty printer

    - /src/test: JUnit tests, with tests for e.j and e1.j (Exclude to build without JUnit)
    - /build.xml: Ant build file
    - /tools and /lib: jars file for running JFlex & Cup, and runtime library for the generated code

The generated code are already included.
To generate them again, run the `generate` task in the Ant build. Some edits need to be done:
    - Add `public` to the `Scanner` class and constructor.

External dependency: JUnit 5.4 (only test)
