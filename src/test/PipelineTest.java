import ir3.ast.Program3;
import org.junit.jupiter.api.Test;

import java.io.FileReader;

import static org.junit.jupiter.api.Assertions.*;

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
        try {
            var in = new FileReader(filename);
            Program3 ir3Code = Pipeline.ir3Gen(in);
            System.out.println(ir3Code);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    void typeCheck1() {
        typeCheck("jlite_programs/sample.j");
    }

    @Test
    void ir3Gen1() {
        ir3Gen("jlite_programs/sample.j");
    }

    @Test
    void ir3Gen2() {
        ir3Gen("jlite_programs/e.j");
    }

    @Test
    void ir3Gen3() {
        ir3Gen("jlite_programs/e1.j");
    }

    @Test
    void ir3Gen4() {
        ir3Gen("jlite_programs/sample1.j");
    }
}