package ir3.ast;

import java.util.List;
import java.util.stream.Collectors;

public abstract class Expr3 implements Ir3Node {
    public static class FnCall extends Expr3 {
        final Id3 fn;
        final List<Id3> args;

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
        final Id3 obj;
        final Id3 field;

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
        final Id3 id;

        public IdExpr(Id3 id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return id.name;
        }
    }

    public static class NewExpr extends Expr3 {
        final Type3 type;

        public NewExpr(Type3 type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return "new " + type.name + "()";
        }
    }

    public static class BoolConst extends Expr3 {
        final boolean value;

        public BoolConst(boolean value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    public static class IntConst extends Expr3 {
        final int value;

        public IntConst(int value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    public static class StringConst extends Expr3 {
        final String value;

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
}
