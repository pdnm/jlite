package ir3.ast;

import ast.Type;

public class Type3 implements Ir3Node {
    final String name;

    public Type3(String name) {
        this.name = name;
    }

    public static Type3 fromType(Type t) {
        return new Type3(t.name);
    }
}
