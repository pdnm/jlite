package ir3.ast;

import ast.Identifier;

public class Id3 implements Ir3Node {
    final String name;

    public Id3(String name) {
        this.name = name;
    }

    public static Id3 fromId(Identifier id) {
        return new Id3(id.name);
    }

    @Override
    public String toString() {
        return name;
    }
}
