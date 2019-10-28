package ir3.ast;

import java.util.List;

public abstract class Stmt3 implements Ir3Node {
    public static class Label extends Stmt3 {
        final int id;

        public Label(int id) {
            this.id = id;
        }
    }

    public static class If extends Stmt3 {
        final Id3 cond;
        final Label target;

        public If(Id3 cond, Label target) {
            this.cond = cond;
            this.target = target;
        }
    }

    public static class Goto extends Stmt3 {
        final Label target;

        public Goto(Label target) {
            this.target = target;
        }
    }

    public static class Asn extends Stmt3 {
        final Id3 lvalue;
        final Expr3 rvalue;

        public Asn(Id3 lvalue, Expr3 rvalue) {
            this.lvalue = lvalue;
            this.rvalue = rvalue;
        }
    }

    public static class FdAsn extends Stmt3 {
        final Id3 obj;
        final Id3 field;
        final Expr3 rvalue;

        public FdAsn(Id3 obj, Id3 field, Expr3 rvalue) {
            this.obj = obj;
            this.field = field;
            this.rvalue = rvalue;
        }
    }

    public static class FnCall extends Stmt3 {
        final Id3 fn;
        final List<Id3> args;

        public FnCall(Id3 fn, List<Id3> args) {
            this.fn = fn;
            this.args = args;
        }
    }

    public static class Return extends Stmt3 {
        final Id3 value;

        public Return(Id3 value) {
            this.value = value;
        }
    }
}
