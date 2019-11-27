package ast;

import java.util.List;

public class MdDecl implements AstNode {
    public final Type rtype;
    public final Identifier name;
    public final List<FmlParam> params;
    public final List<VarDecl> vars;
    public final List<Stmt> stmts;

    public MdDecl(Type rtype, Identifier name, List<FmlParam> params, List<VarDecl> vars, List<Stmt> stmts)
            throws Exception {
        if (stmts.isEmpty()) {
            throw new Exception("A method needs at least one statement");
        }
        this.rtype = rtype;
        this.name = name;
        this.params = params;
        this.vars = vars;
        this.stmts = stmts;
    }
}
