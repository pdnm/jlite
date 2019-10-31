package ir3;

import ast.Identifier;
import ast.Type;
import ir3.ast.Id3;
import ir3.ast.Type3;
import type_check.ClassEnv;
import type_check.Context;

import java.util.List;
import java.util.Optional;

public class Context3 {
    final Context ctx;

    static final Type3 boolType = new Type3("Bool");
    static final Id3 thisId = new Id3("this");
    static final Id3 voidId = new Id3("void");

    public Context3(Context ctx) {
        this.ctx = ctx;
    }

    private static Optional<Id3> getId3Function(ClassEnv cls, Identifier name, List<Type> argTypes) {
        if (cls.getMethodReturnType(name, argTypes).isEmpty())
            return Optional.empty();
        var position = cls.getMethodOverloadPosition(name, argTypes);
        if (position == -1) return Optional.empty();
        return Optional.of(new Id3("%" + cls.name + "_" + name.name + "%" + position));
    }

    Optional<Id3> getId3Function(Type cls, Identifier name, List<Type> argTypes) {
        return ctx.classDesc.getClassEnv(cls).flatMap(env -> getId3Function(env , name, argTypes));
    }

    Optional<Id3> getId3LocalFunction(Identifier name, List<Type> argTypes) {
        return getId3Function(ctx.localClass, name, argTypes);
    }

    Optional<Id3> getId3BuiltinFunction(Identifier name, List<Type> argTypes) {
        return getId3Function(ctx.pervasive, name, argTypes);
    }
}
