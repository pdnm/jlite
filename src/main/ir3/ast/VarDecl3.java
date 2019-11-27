package ir3.ast;

import ast.VarDecl;

public class VarDecl3 implements Ir3Node {
    public final Type3 type;
    public final Id3 name;

    public VarDecl3(Type3 type, Id3 name) {
        this.type = type;
        this.name = name;
    }

    public static VarDecl3 fromVarDecl(VarDecl v) {
        return new VarDecl3(Type3.fromType(v.type), Id3.fromId(v.name));
    }

    @Override
    public void display(StringBuilder sb, int indent) {
        sb.append(" ".repeat(indent)).append(type.name + " " + name.name + ";\n");
    }
}
