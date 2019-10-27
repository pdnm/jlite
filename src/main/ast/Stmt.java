package ast;

import java.util.List;
import java.util.function.Function;

public abstract class Stmt implements AstNode {
    public static class Assignment extends Stmt {
        public final Expr lvalue;
        public final Expr rvalue;

        public Assignment(Expr lvalue, Expr rvalue) {
            this.lvalue = lvalue;
            this.rvalue = rvalue;
        }
    }
    public static Assignment assignment(Expr lvalue, Expr rvalue) {
        return new Assignment(lvalue, rvalue);
    }


    public static class IfStmt extends Stmt {
        public final Expr cond;
        public final List<Stmt> then;
        public final List<Stmt> alt;

        public IfStmt(Expr cond, List<Stmt> then, List<Stmt> alt) {
            this.cond = cond;
            this.then = then;
            this.alt = alt;
        }
    }
    public static IfStmt ifStmt(Expr cond, List<Stmt> then, List<Stmt> alt) throws Exception {
        if (then.isEmpty()) {
            throw new Exception("The body of `if` needs at least one statement");
        }
        if (alt.isEmpty()) {
            throw new Exception("The body of `else` needs at least one statement");
        }
        return new IfStmt(cond, then, alt);
    }

    public static class FnCall extends Stmt {
        public final Expr fn;
        public final List<Expr> args;

        public FnCall(Expr fn, List<Expr> args) {
            this.fn = fn;
            this.args = args;
        }
    }
    public static FnCall fnCall(Expr fn, List<Expr> args) {
        return new FnCall(fn, args);
    }

    public static class Return extends Stmt {
        public final Expr expr;

        public Return(Expr expr) {
            this.expr = expr;
        }
    }
    public static Return returnStmt(Expr expr) {
        return new Return(expr);
    }

    public static class ReturnVoid extends Stmt {
    }
    public static ReturnVoid returnStmt() {
        return new ReturnVoid();
    }

    public static class WhileStmt extends Stmt {
        public final Expr cond;
        public final List<Stmt> block;

        public WhileStmt(Expr cond, List<Stmt> block) {
            this.cond = cond;
            this.block = block;
        }
    }
    public static WhileStmt whileStmt(Expr cond, List<Stmt> block) {
        return new WhileStmt(cond, block);
    }

    public <T> T process(Function<IfStmt, T> ifF,
                         Function<WhileStmt, T> whileF,
                         Function<FnCall, T> callF,
                         Function<Assignment, T> asnF,
                         Function<Return, T> retF,
                         Function<ReturnVoid, T> retVoidF) {
        if (this instanceof Stmt.IfStmt) {
            var s = (Stmt.IfStmt) this;
            return ifF.apply(s);
        } else if (this instanceof Stmt.WhileStmt) {
            var s = (Stmt.WhileStmt) this;
            return whileF.apply(s);
        } else if (this instanceof Stmt.FnCall) {
            var s = (Stmt.FnCall) this;
            return callF.apply(s);
        } else if (this instanceof Stmt.Assignment) {
            var s = (Stmt.Assignment) this;
            return asnF.apply(s);
        } else if (this instanceof Stmt.Return) {
            var s = (Stmt.Return) this;
            return retF.apply(s);
        } else {
            var s = (Stmt.ReturnVoid) this;
            return retVoidF.apply(s);
        }
    }
}
