package parser;


import java_cup.runtime.Symbol;
import parser.gen.Scanner;
import parser.gen.sym;
import processor.PrettyPrinter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

class ParserDriverTest {
    void scannerTest(String filename) {
        try {
            var in = new FileReader(filename);
            var sc = new Scanner(in);
            var symbols = new ArrayList<>();
            while (true) {
                try {
                    Symbol s = sc.next_token();
                    symbols.add(sym.terminalNames[s.sym]);
                    if (s.sym == 0) break;
                } catch (IOException e) {
                    break;
                }
            }
            symbols.forEach(System.out::println);
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }

    void prettyPrintingTest(String filename) {
        try {
            var in = new FileReader(filename);
            var p = new ParserDriver(in);
            var sb = new StringBuilder();
            var pp = new PrettyPrinter(sb::append, 4);
            pp.programPrinter(p.parse(), 0);
            System.out.println(sb.toString());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    void test1() {
        prettyPrintingTest("testcases/sample1.j");
    }

    void test2() {
        prettyPrintingTest("testcases/sample5.j");
    }
}