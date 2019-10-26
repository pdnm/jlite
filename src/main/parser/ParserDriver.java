package parser;

import ast.Program;
import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.Symbol;
import java_cup.runtime.SymbolFactory;
import parser.gen.Scanner;
import parser.gen.sym;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class ParserDriver {
    private final parser.gen.Parser parser;

    public ParserDriver(Reader reader) {
        SymbolFactory sf = new ComplexSymbolFactory();
        this.parser = new parser.gen.Parser(new Scanner(reader));
    }

    public Program parse() throws Exception {
        return (Program)this.parser.parse().value;
    }
}
