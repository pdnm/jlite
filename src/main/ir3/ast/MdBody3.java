package ir3.ast;

import java.util.List;

public class MdBody3 {
    final List<VarDecl3> vars;
    final List<Stmt3> stmts;

    public MdBody3(List<VarDecl3> vars, List<Stmt3> stmts) {
        this.vars = vars;
        this.stmts = stmts;
    }
}
