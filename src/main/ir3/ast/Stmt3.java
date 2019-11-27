package ir3.ast;

import ast.AstNode;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class Stmt3 implements Ir3Node {
    public static class Label extends Stmt3 {
        public final int id;

        public Label(int id) {
            this.id = id;
        }

        @Override
        public void display(StringBuilder sb, int indent) {
            sb.append(" ".repeat(indent)).append("Label " + id + ":\n");
        }
    }

    public static class If extends Stmt3 {
        public final Id3 cond;
        public final Label target;

        public If(Id3 cond, Label target) {
            this.cond = cond;
            this.target = target;
        }

        @Override
        public void display(StringBuilder sb, int indent) {
            sb.append(" ".repeat(indent)).append("if (" + cond.toString() + ") Goto " + target.id +  ";\n");
        }
    }

    public static class Goto extends Stmt3 {
        public final Label target;

        public Goto(Label target) {
            this.target = target;
        }

        @Override
        public void display(StringBuilder sb, int indent) {
            sb.append(" ".repeat(indent)).append("Goto " + target.id + ";\n");
        }
    }

    public static class Asn extends Stmt3 {
        public final Id3 lvalue;
        public final Expr3 rvalue;

        public Asn(Id3 lvalue, Expr3 rvalue) {
            this.lvalue = lvalue;
            this.rvalue = rvalue;
        }

        @Override
        public void display(StringBuilder sb, int indent) {
            sb.append(" ".repeat(indent)).append(lvalue.name + " = " + rvalue.toString() + ";\n");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Asn asn = (Asn) o;
            return lvalue.equals(asn.lvalue) &&
                    rvalue.equals(asn.rvalue);
        }

        @Override
        public int hashCode() {
            return Objects.hash(lvalue, rvalue);
        }
    }

    public static class FdAsn extends Stmt3 {
        public final Id3 obj;
        public final Id3 field;
        public final Expr3 rvalue;

        public FdAsn(Id3 obj, Id3 field, Expr3 rvalue) {
            this.obj = obj;
            this.field = field;
            this.rvalue = rvalue;
        }

        @Override
        public void display(StringBuilder sb, int indent) {
            sb.append(" ".repeat(indent)).append(obj.name + "." + field.name + " = " + rvalue.toString() + ";\n");
        }
    }

    public static class FnCall extends Stmt3 {
        public final Id3 fn;
        public final List<Id3> args;

        public FnCall(Id3 fn, List<Id3> args) {
            this.fn = fn;
            this.args = args;
        }

        @Override
        public void display(StringBuilder sb, int indent) {
            sb.append(" ".repeat(indent))
                    .append(fn.name + "(" + args.stream().map(Id3::toString).collect(Collectors.joining(", ")) + ");\n");
        }
    }

    public static class Return extends Stmt3 {
        public final Id3 value;

        public Return(Id3 value) {
            this.value = value;
        }

        @Override
        public void display(StringBuilder sb, int indent) {
            sb.append(" ".repeat(indent)).append("return " + value.name + ";\n");
        }
    }

    public <T> T process(Function<Label, T> labelTFunction,
                         Function<If, T> ifTFunction,
                         Function<Goto, T> gotoTFunction,
                         Function<Asn, T> asnTFunction,
                         Function<FdAsn, T> fdAsnTFunction,
                         Function<FnCall, T> fnCallTFunction,
                         Function<Return, T> returnTFunction) {
        if (this instanceof Label) return labelTFunction.apply((Label) this);
        else if (this instanceof If) return ifTFunction.apply((If) this);
        else if (this instanceof Goto) return gotoTFunction.apply((Goto) this);
        else if (this instanceof Asn) return asnTFunction.apply((Asn) this);
        else if (this instanceof FdAsn) return fdAsnTFunction.apply((FdAsn) this);
        else if (this instanceof FnCall) return fnCallTFunction.apply((FnCall) this);
        else return returnTFunction.apply((Return) this);
    }
}
