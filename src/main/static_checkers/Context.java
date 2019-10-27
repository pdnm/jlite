package static_checkers;

import ast.Identifier;
import ast.Operator;
import ast.Type;

import java.util.*;

import static java.util.Map.entry;
import static ast.Operator.*;

public class Context {
    final ClassDesc classDesc;
    final TypeEnv typeEnv;
    final ClassEnv localClass;
    private final Set<Type> classes;
    private final Set<Type> types;

    static final Type intType = new Type("Int");
    static final Type boolType = new Type("Bool");
    static final Type stringType = new Type("String");
    static final Type voidType = new Type("Void");

    static class BinOpType {
        Type left, right, result;

        public BinOpType(Type left, Type right, Type result) {
            this.left = left;
            this.right = right;
            this.result = result;
        }
    }

    static class UnOpType {
        Type operand, result;

        public UnOpType(Type arg, Type result) {
            this.operand = arg;
            this.result = result;
        }
    }

    static final Map<Operator, BinOpType> binOps;
    static final Map<Operator, UnOpType> unOps;
    static {
        var arithType = new BinOpType(intType, intType, intType);
        var compareType = new BinOpType(intType, intType, boolType);
        var logicType = new BinOpType(boolType, boolType, boolType);
        binOps = Map.ofEntries(
                entry(ADD, arithType),
                entry(SUB, arithType),
                entry(MUL, arithType),
                entry(DIV, arithType),
                entry(EQ, compareType),
                entry(NEQ, compareType),
                entry(GT, compareType),
                entry(LT, compareType),
                entry(GE, compareType),
                entry(LE, compareType),
                entry(AND, logicType),
                entry(OR, logicType)
        );

        unOps = Map.ofEntries(
                entry(UMINUS, new UnOpType(intType, intType)),
                entry(NEG, new UnOpType(boolType, boolType))
        );
    }

    public Context(ClassDesc classDesc, TypeEnv typeEnv, ClassEnv localClass, Set<Type> classes, Set<Type> types) {
        this.classDesc = classDesc;
        this.typeEnv = typeEnv;
        this.localClass = localClass;
        this.classes = classes;
        this.types = types;
    }

    Context(ClassDesc classDesc, Type cls) {
        this.classDesc = classDesc;
        this.typeEnv = TypeEnv.emptyEnv();
        this.localClass = this.classDesc.getClassEnv(cls).get();
        this.classes = new HashSet<>(this.classDesc.getAllClassTypes());
        this.types = new HashSet<>(this.classDesc.getAllClassTypes());
        this.types.addAll(primitiveTypes());
    }

    Context extend(Identifier id, Type t) {
        return new Context(classDesc, typeEnv.extend(id, t), localClass, classes, types);
    }

    Optional<Type> getType(Identifier id) {
        return typeEnv.getType(id).or(() -> localClass.getFieldType(id));
    }

    private List<Type> primitiveTypes() {
        return List.of(intType, boolType, stringType, voidType);
    }

    boolean isLegitimate(Type type) {
        return types.contains(type);
    }

    boolean isClass(Type type) {
        return classes.contains(type);
    }
}
