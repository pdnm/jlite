package ir3.ast;

import ast.Operator;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class Expr3 implements Ir3Node {
    public static class BinOp extends Expr3 {
        public final Operator op;
        public final Id3 left;
        public final Id3 right;

        public BinOp(Operator op, Id3 left, Id3 right) {
            this.op = op;
            this.left = left;
            this.right = right;
        }
    }

    public static class UnOp extends Expr3 {
        public final Operator op;
        public final Id3 operand;

        public UnOp(Operator op, Id3 operand) {
            this.op = op;
            this.operand = operand;
        }
    }

    public static class FnCall extends Expr3 {
        public final Id3 fn;
        public final List<Id3> args;

        public FnCall(Id3 fn, List<Id3> args) {
            this.fn = fn;
            this.args = args;
        }

        @Override
        public String toString() {
            return fn.name + "(" + args.stream().map(Id3::toString).collect(Collectors.joining(", ")) + ")";
        }
    }

    public static class FieldAccess extends Expr3 {
        public final Id3 obj;
        public final Id3 field;

        public FieldAccess(Id3 obj, Id3 field) {
            this.obj = obj;
            this.field = field;
        }

        @Override
        public String toString() {
            return obj.name + "." + field.name;
        }
    }

    public static class IdExpr extends Expr3 {
        public final Id3 id;

        public IdExpr(Id3 id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return id.name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            IdExpr idExpr = (IdExpr) o;
            return id.equals(idExpr.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }

    public static class NewExpr extends Expr3 {
        public final Type3 type;

        public NewExpr(Type3 type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return "new " + type.name + "()";
        }
    }

    public static class BoolConst extends Expr3 {
        public final boolean value;

        public BoolConst(boolean value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    public static class IntConst extends Expr3 {
        public final int value;

        public IntConst(int value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    public static class StringConst extends Expr3 {
        public final String value;

        public StringConst(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static class VoidValue extends Expr3 {
        @Override
        public String toString() {
            return "()";
        }
    }

    public static class Null extends Expr3 {
        @Override
        public String toString() {
            return "null";
        }
    }

    /**
     * @return List of variables in the expression
     */
    public List<Id3> vars() {
        if (this instanceof BinOp) return List.of(((BinOp) this).left, ((BinOp) this).right);
        else if (this instanceof UnOp) return List.of(((UnOp) this).operand);
        else if (this instanceof FnCall) return ((FnCall) this).args;
        else if (this instanceof FieldAccess) return List.of(((FieldAccess) this).obj);
        else if (this instanceof IdExpr) return List.of(((IdExpr) this).id);
        else return List.of();
    }

    public <T> T process(
            Function<BinOp, T> binOpTFunction,
            Function<UnOp, T> unOpTFunction,
            Function<FnCall, T> fnCallTFunction,
            Function<FieldAccess, T> fieldAccessTFunction,
            Function<IdExpr, T> idExprTFunction,
            Function<NewExpr, T> newExprTFunction,
            Function<BoolConst, T> boolConstTFunction,
            Function<IntConst, T> intConstTFunction,
            Function<StringConst, T> stringConstTFunction,
            Function<VoidValue, T> voidValueTFunction
    ) {
        if (this instanceof BinOp)
            return binOpTFunction.apply((BinOp) this);
        else if (this instanceof UnOp)
            return unOpTFunction.apply((UnOp) this);
        else if (this instanceof FnCall)
            return fnCallTFunction.apply((FnCall) this);
        else if (this instanceof FieldAccess)
            return fieldAccessTFunction.apply((FieldAccess) this);
        else if (this instanceof IdExpr)
            return idExprTFunction.apply((IdExpr) this);
        else if (this instanceof NewExpr)
            return newExprTFunction.apply((NewExpr) this);
        else if (this instanceof BoolConst)
            return boolConstTFunction.apply((BoolConst) this);
        else if (this instanceof IntConst)
            return intConstTFunction.apply((IntConst) this);
        else if (this instanceof StringConst)
            return stringConstTFunction.apply((StringConst) this);
        else if (this instanceof VoidValue)
            return voidValueTFunction.apply((VoidValue) this);
        else throw new RuntimeException("Null is forbidden");
    }
}
