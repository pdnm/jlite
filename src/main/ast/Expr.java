package ast;

import java.util.List;
import java.util.function.Function;

public abstract class Expr implements AstNode {
    Type type;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public static class BinOp extends Expr {
        public final Operator op;
        public final Expr left;
        public final Expr right;

        public BinOp(Operator op, Expr left, Expr right) {
            this.op = op;
            this.left = left;
            this.right = right;
        }
    }
    public static BinOp binOp(Operator op, Expr left, Expr right) {
        return new BinOp(op, left, right);
    }

    public static class UnOp extends Expr {
        public final Operator op;
        public final Expr expr;

        public UnOp(Operator op, Expr expr) {
            this.op = op;
            this.expr = expr;
        }
    }
    public static UnOp unOp(Operator op, Expr expr) {
        return new UnOp(op, expr);
    }

    public static class Path extends Expr {
        public final Expr left;
        public final Identifier id;

        @Override
        public String toString() {
            return "Path{" +
                    "left=" + left +
                    ", id=" + id +
                    '}';
        }

        public Path(Expr left, Identifier id) {
            this.left = left;
            this.id = id;
        }
    }
    public static Path path(Expr left, Identifier id) {
        return new Path(left, id);
    }

    public static  class FnCall extends Expr {
        public final Expr fn;
        public final List<Expr> args;

        @Override
        public String toString() {
            return "FnCall{" +
                    "fn=" + fn +
                    ", args=" + args +
                    '}';
        }

        public FnCall(Expr fn, List<Expr> args) {
            this.fn = fn;
            this.args = args;
        }
    }
    public static FnCall fnCall(Expr fn, List<Expr> args) { return new FnCall(fn, args); }

    public static class IntLit extends Expr {
        public final int value;

        public IntLit(int value) {
            this.value = value;
        }
    }
    public static IntLit intLit(int value) { return new IntLit(value); }

    public static class StringLit extends Expr {
        public final String value;

        public StringLit(String value) {
            this.value = value;
        }
    }
    public static StringLit stringLit(String value) { return new StringLit(value); }

    public static class BoolLit extends Expr {
        public final boolean value;

        public BoolLit(boolean value) {
            this.value = value;
        }
    }
    public static BoolLit boolLit(boolean value) { return new BoolLit(value); }

    public static class IdExpr extends Expr {
        public final Identifier id;

        @Override
        public String toString() {
            return "IdExpr{" +
                    "id=" + id +
                    '}';
        }

        public IdExpr(Identifier id) {
            this.id = id;
        }
    }
    public static IdExpr idExpr(Identifier id) { return new IdExpr(id); }

    public static class ThisExpr extends Expr {
        public ThisExpr() {
        }

    }
    public static ThisExpr thisExpr() { return new ThisExpr(); }

    public static class NewExpr extends Expr {
        public final Type className;

        public NewExpr(Type className) {
            this.className = className;
        }
    }
    public static NewExpr newExpr(Type className) { return new NewExpr(className); }

    public static class Null extends Expr {
        public Null() {
        }
    }
    public static Null nullExpr() { return new Null(); }

    public <T> T process(Function<BoolLit, T> boolLitTFunction,
                         Function<IntLit, T> intLitTFunction,
                         Function<StringLit, T> stringLitTFunction,
                         Function<BinOp, T> binOpTFunction,
                         Function<UnOp, T> unOpTFunction,
                         Function<IdExpr, T> idExprTFunction,
                         Function<NewExpr, T> newExprTFunction,
                         Function<ThisExpr, T> thisExprTFunction,
                         Function<Null, T> nullTFunction,
                         Function<FnCall, T> fnCallTFunction,
                         Function<Path, T> pathTFunction) {
        if (this instanceof Expr.BoolLit) {
            var e = (Expr.BoolLit) this;
            return boolLitTFunction.apply(e);
        } else if (this instanceof Expr.IntLit) {
            var e = (Expr.IntLit) this;
            return intLitTFunction.apply(e);
        } else if (this instanceof Expr.StringLit) {
            var e = (Expr.StringLit) this;
            return stringLitTFunction.apply(e);
        } else if (this instanceof Expr.BinOp) {
            var e = (Expr.BinOp) this;
            return binOpTFunction.apply(e);
        } else if (this instanceof Expr.UnOp) {
            var e = (Expr.UnOp) this;
            return unOpTFunction.apply(e);
        } else if (this instanceof Expr.IdExpr) {
            var e = (Expr.IdExpr) this;
            return idExprTFunction.apply(e);
        } else if (this instanceof Expr.NewExpr) {
            var e = (Expr.NewExpr) this;
            return newExprTFunction.apply(e);
        } else if (this instanceof Expr.ThisExpr) {
            var e = (Expr.ThisExpr) this;
            return thisExprTFunction.apply(e);
        } else if (this instanceof Expr.Null) {
            var e = (Expr.Null) this;
            return nullTFunction.apply(e);
        } else if (this instanceof Expr.FnCall) {
            var e = (Expr.FnCall) this;
            return fnCallTFunction.apply(e);
        } else /*if (this instanceof Expr.Path)*/ {
            var e = (Expr.Path) this;
            return pathTFunction.apply(e);
        }
    }
}
