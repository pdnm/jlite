package ast;

public class FmlParam implements AstNode {
    public final Type type;
    public final Identifier name;

    public FmlParam(Type type, Identifier name) {
        this.type = type;
        this.name = name;
    }
}
