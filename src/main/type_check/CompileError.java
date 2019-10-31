package type_check;

import ast.*;

public class CompileError {
    private final String message;
    private final AstNode astNode;

    CompileError(String message, AstNode astNode) {
        this.message = message;
        this.astNode = astNode;
    }

    @Override
    public String toString() {
        return message + "; Occurs at " + astNode;
    }

    static CompileError typeNotDefined(Type type) {
        return new CompileError("Type " + type + " is not defined", type);
    }

    static CompileError duplicatedVarNames(MdDecl md) {
        return new CompileError("Duplicated variable names in method", md);
    }

    static CompileError nonBooleanCondition(Expr expr, Type t) {
        return new CompileError("Expected condition type Bool, found " + t, expr);
    }

    static CompileError branchTypeMismatch(Stmt stmt) {
        return new CompileError("Mismatch between types of an if statement", stmt);
    }

    static CompileError methodNotDefined(AstNode call) {
        return new CompileError("Cannot resolve method call", call);
    }

    static CompileError fieldNotDefined(Identifier id, Type t) {
        return new CompileError(String.format("Field %s is not defined for type %s", id, t), id);
    }

    static CompileError notAMethod(Expr expr) {
        return new CompileError("Expression is not a method", expr);
    }

    static CompileError identifierNotDef(Identifier id) {
        return new CompileError("Identifier " + id + " is not defined", id);
    }

    static CompileError notLvalue(Expr e) {
        return new CompileError("Expression is not assignable", e);
    }

    static CompileError assMismatch(Stmt.Assignment a, Type t1, Type t2) {
        return new CompileError("Assignment type mismatch, expected type " + t1 + " found type " + t2, a);
    }

    static CompileError retTypeMismatch(MdDecl a, Type t1, Type t2) {
        return new CompileError("Return type mismatch, expected type " + t1 + " found type " + t2, a);
    }

    static CompileError binOpTypesMismatch(Expr e, Operator op, Type l1, Type r1, Type l2, Type r2) {
        return new CompileError(
                String.format("Operand types mismatch, expected type %s %s %s, found %s %s %s",
                        l1, op, r1, l2, op, r2) , e);
    }

    static CompileError unOpTypeMismatch(Expr e, Operator op, Type t1, Type t2) {
        return new CompileError(
                String.format("Operand type mismatch, expected type %s %s, found %s %s",
                        op, t1, op, t2) , e);
    }

    static CompileError notClass(Type t) {
        return new CompileError(String.format("Type %s is not a class", t), t);
    }

    static CompileError noNull(Expr e) {
        return new CompileError("Null is forbidden", e);
    }
}
