import ast.Program;
import backend.ArmISA;
import backend.ArmTranslator;
import ir3.Translator;
import ir3.ast.Program3;
import parser.ParserDriver;
import type_check.TypeChecker;

import java.io.Reader;
import java.util.List;
import java.util.stream.Collectors;

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

    static List<ArmISA.Instr> compileArm(Reader source) {
        Program3 program3 = ir3Gen(source);
        var translator = new ArmTranslator(4, 4, 4, 8, 11, 13, 14, 15);
        var instructions = translator.translate(program3);
        return instructions.stream().filter(instr ->
                   !(instr instanceof ArmISA.Push && ((ArmISA.Push) instr).registers.isEmpty())
                && !(instr instanceof ArmISA.Pop &&  ((ArmISA.Pop)  instr).registers.isEmpty())
        ).collect(Collectors.toList());
    }
}
