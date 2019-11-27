package ir3.ast;

import ast.FmlParam;

import java.util.List;
import java.util.stream.Collectors;

public class FmlParam3 {
    public final Type3 type;
    public final Id3 name;

    public FmlParam3(Type3 type, Id3 name) {
        this.type = type;
        this.name = name;
    }

    public static List<FmlParam3> fromFmlParams(List<FmlParam> params) {
        return params.stream()
                .map(param -> new FmlParam3(Type3.fromType(param.type), Id3.fromId(param.name)))
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return type.name + " " + name.name;
    }
}
