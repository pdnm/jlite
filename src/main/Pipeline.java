import ast.Program;
import ir3.Translator;
import ir3.ast.Program3;
import parser.ParserDriver;
import static_checkers.TypeChecker;

import java.io.Reader;

public class Pipeline {
    private static Program parse(Reader source) {
        var p = new ParserDriver(source);
        var sb = new StringBuilder();
        try {
            return p.parse();
        } catch (Exception e) {
            throw new RuntimeException("Parse error");
        }
    }

    static boolean typeCheck(Reader source) {
        Program program = parse(source);
        var compileErrors = TypeChecker.check(program);
        if (!compileErrors.isEmpty()) {
            for (var error : compileErrors) {
                System.out.println(error.toString());
            }
            return false;
        }
        return true;
    }

    static Program3 ir3Gen(Reader source) {
        Program program = parse(source);
        var compileErrors = TypeChecker.check(program);
        if (!compileErrors.isEmpty()) {
            for (var error : compileErrors) {
                System.out.println(error.toString());
            }
            throw new RuntimeException("Compile error");
        }

        return new Translator().translate(program);
    }
}
