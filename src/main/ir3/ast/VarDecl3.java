package ir3.ast;

import ast.VarDecl;

public class VarDecl3 implements Ir3Node {
    final Type3 type;
    final Id3 name;

    public VarDecl3(Type3 type, Id3 name) {
        this.type = type;
        this.name = name;
    }

    public static VarDecl3 fromVarDecl(VarDecl v) {
        return new VarDecl3(Type3.fromType(v.type), Id3.fromId(v.name));
    }
}
