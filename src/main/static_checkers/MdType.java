package static_checkers;

import ast.MdDecl;
import ast.Type;

import java.util.List;
import java.util.stream.Collectors;

public class MdType {
    public final List<Type> argTypes;
    public final Type retType;

    public MdType(MdDecl mdDecl) {
        this.argTypes = mdDecl.params.stream()
                .map(fmlParam -> fmlParam.type)
                .collect(Collectors.toList());
        this.retType = mdDecl.rtype;
    }
}
