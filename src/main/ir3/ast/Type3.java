package ir3.ast;

import ast.Type;

import java.util.Objects;

public class Type3 implements Ir3Node {
    public final String name;

    public Type3(String name) {
        this.name = name;
    }

    public static Type3 fromType(Type t) {
        return new Type3(t.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Type3 type3 = (Type3) o;
        return name.equals(type3.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
