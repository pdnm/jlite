package ir3.ast;

import java.util.List;

public abstract class Expr3 implements Ir3Node {
    public static class FnCall extends Expr3 {
        final Id3 fn;
        final List<Id3> args;

        public FnCall(Id3 fn, List<Id3> args) {
            this.fn = fn;
            this.args = args;
        }
    }

    public static class FieldAccess extends Expr3 {
        final Id3 obj;
        final Id3 field;

        public FieldAccess(Id3 obj, Id3 field) {
            this.obj = obj;
            this.field = field;
        }
    }

    public static class IdExpr extends Expr3 {
        final Id3 id;

        public IdExpr(Id3 id) {
            this.id = id;
        }
    }

    public static class NewExpr extends Expr3 {
        final Type3 type;

        public NewExpr(Type3 type) {
            this.type = type;
        }
    }

    public static class BoolConst extends Expr3 {
        final boolean value;

        public BoolConst(boolean value) {
            this.value = value;
        }
    }

    public static class IntConst extends Expr3 {
        final int value;

        public IntConst(int value) {
            this.value = value;
        }
    }

    public static class StringConst extends Expr3 {
        final String value;

        public StringConst(String value) {
            this.value = value;
        }
    }

    public static class VoidValue extends Expr3 {
    }

    public static class Null extends Expr3 {
    }
}
