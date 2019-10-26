package ast;

import java.util.List;

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
}
