import backend.ArmISA;
import ir3.ast.Program3;

import java.io.*;
import java.util.List;

class PipelineTest {

    void typeCheck(String filename) {
        try {
            var in = new FileReader(filename);
            Pipeline.typeCheck(in);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    void ir3Gen(String filename) {
        System.out.println("=".repeat(30) + " " + filename + " " + "=".repeat(30));
        try {
            var in = new FileReader(filename);
            Program3 ir3Code = Pipeline.ir3Gen(in);
            System.out.println(ir3Code);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    void compile(String sourceFile, String outputFile) {
        try {
            var in = new FileReader(sourceFile);
            var out = new PrintWriter(new FileWriter(outputFile));
            List<ArmISA.Instr> nativeCode = Pipeline.compileArm(in);
            for (var instr : nativeCode) {
                if (!(instr instanceof ArmISA.Label))
                   out.print("\t");
                out.println(instr);
            }
            in.close();
            out.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    void typeCheck1() {
        typeCheck("testcases/sample1.j");
    }

    void ir3Gen1() {
        ir3Gen("testcases/sample1.j");
    }

    void ir3Gen2() {
        ir3Gen("testcases/sample2.j");
    }
    void ir3Gen3() {
        ir3Gen("testcases/sample3.j");
    }

    void compile1() {
        compile("testcases/sample1.j", "testcases/sample1.s");
    }
    void compile2() {
        compile("testcases/sample2.j", "testcases/sample2.s");
    }
    void compile3() {
        compile("testcases/sample3.j", "testcases/sample3.s");
    }
    void compile4() {
        compile("testcases/sample4.j", "testcases/sample4.s");
    }
    void compile5() {
        compile("testcases/sample5.j", "testcases/sample5.s");
    }

    public static void main(String[] args) {
        var tests = new PipelineTest();
        tests.compile1();
        tests.compile2();
        tests.compile3();
        tests.compile4();
        tests.compile5();
    }
}