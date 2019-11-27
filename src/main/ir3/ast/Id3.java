package ir3.ast;

import ast.Identifier;

import java.util.Objects;

public class Id3 implements Ir3Node {
    public final String name;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Id3 id3 = (Id3) o;
        return name.equals(id3.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
