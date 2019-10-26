package ast;

public class VarDecl implements AstNode {
    public final Type type;
    public final Identifier name;

    public VarDecl(Type type, Identifier name) {
        this.type = type;
        this.name = name;
    }
}
