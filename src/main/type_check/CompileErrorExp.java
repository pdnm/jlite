package type_check;

import java.util.List;

public class CompileErrorExp extends Exception {
    public final List<CompileError> errors;

    public CompileErrorExp(List<CompileError> errors) {
        this.errors = errors;
    }

    public CompileErrorExp(CompileError error) {
        this.errors = List.of(error);
    }
}
