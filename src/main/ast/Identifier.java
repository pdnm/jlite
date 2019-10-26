package ast;

public class Identifier implements AstNode {
    public final String name;

    public Identifier(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
