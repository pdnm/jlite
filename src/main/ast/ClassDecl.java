package ast;


import java.util.Collections;
import java.util.List;

public class ClassDecl implements AstNode {
    public final Type name;
    public final List<VarDecl> vars;
    public final List<MdDecl> mds;

    public ClassDecl(Type name, List<VarDecl> vars, List<MdDecl> mds) {
        this.name = name;
        this.vars = vars;
        this.mds = mds;
    }
}
