package ast;

public class Type implements AstNode {
    private String name;

    public Type(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
