package static_checkers;

import ast.ClassDecl;
import ast.Identifier;
import ast.Type;
import utils.ListUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.*;

public class ClassEnv {
    public final Type name;
    private Map<String, Type> fields;
    private Map<String, List<MdType>> methods;

    private ClassEnv(Type name, Map<String, Type> fields, Map<String, List<MdType>> methods) {
        this.name = name;
        this.fields = fields;
        this.methods = methods;
    }

    static ClassEnv initialize(ClassDecl clsDecl) throws CompileErrorExp {
        if (ListUtils.isNotUnique(clsDecl.vars, varDecl -> varDecl.name.name))
            throw new CompileErrorExp(new CompileError("Duplicated class field names", clsDecl));
        var fields = clsDecl.vars.stream().collect(toMap(
                varDecl -> varDecl.name.name,
                varDecl -> varDecl.type
        ));
        var methodGroups = clsDecl.mds.stream()
                .collect(groupingBy(
                        mdDecl -> mdDecl.name.name,
                        mapping(MdType::new, toList())));
        for (var group : methodGroups.values()) {
            if (ListUtils.isNotUnique(group, mdType -> mdType.argTypes))
                throw new CompileErrorExp(new CompileError(
                        "Overloaded methods with the same list of argument types", clsDecl));
        }
        for (var md : clsDecl.mds) {
            if (ListUtils.isNotUnique(md.params, fmlParam -> fmlParam.name.name))
                throw new CompileErrorExp(new CompileError(
                        "Duplicated parameter names in method", md));
        }
        return new ClassEnv(clsDecl.name, fields, methodGroups);
    }

    Optional<Type> getFieldType(Identifier name) {
        return fields.containsKey(name.name)
                ? Optional.of(fields.get(name.name))
                : Optional.empty();
    }

    Optional<Type> getMethodReturnType(Identifier name, List<Type> argTypes) {
        if (!methods.containsKey(name.name))
            return Optional.empty();
        var overloadedList = methods.get(name.name);
        return overloadedList
                .stream()
                .filter(mdType -> {
                    return mdType.argTypes.equals(argTypes);
                })
                .findFirst()
                .map(mdType -> mdType.retType);
    }
}
