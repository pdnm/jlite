package ir3.ast;

import java.util.List;

public class MdBody3 implements Ir3Node {
    final List<VarDecl3> vars;
    final List<Stmt3> stmts;

    public MdBody3(List<VarDecl3> vars, List<Stmt3> stmts) {
        this.vars = vars;
        this.stmts = stmts;
    }

    @Override
    public void display(StringBuilder sb, int indent) {
        for (var v : vars) {
            v.display(sb, indent);
        }
        for (var st : stmts) {
            st.display(sb, indent);
        }
    }
}
