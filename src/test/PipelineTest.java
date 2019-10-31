import ir3.ast.Program3;

import java.io.FileReader;

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

    void typeCheck1() {
        typeCheck("jlite_programs/sample1.j");
    }

    void ir3Gen1() {
        ir3Gen("jlite_programs/sample1.j");
    }

    void ir3Gen2() {
        ir3Gen("jlite_programs/sample2.j");
    }

    public static void main(String[] args) {
        var tests = new PipelineTest();
        tests.ir3Gen1();
        System.out.println();
        tests.ir3Gen2();
    }
}