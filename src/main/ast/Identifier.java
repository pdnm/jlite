package ast;

public class Identifier implements AstNode {
    public final String name;
    boolean fromLocalScope;

    public boolean isFromLocalScope() {
        return fromLocalScope;
    }

    public void setFromLocalScope(boolean fromLocalScope) {
        this.fromLocalScope = fromLocalScope;
    }

    public Identifier(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
