package processor;

import ast.*;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class PrettyPrinter {
    private Consumer<String> w;
    private int indentWidth;

    private String tab(int indent) {
        return " ".repeat(indent);
    }

    private int nextLevel(int indent) {
        return indent + this.indentWidth;
    }

    private static <T> String mkString(List<T> xs, Function<T, String> mapper, String start, String sep, String end) {
        var sb = new StringBuilder();
        sb.append(start);
        var first = true;
        for (var x : xs) {
            if (!first) {
                sb.append(sep);
            }
            sb.append(mapper.apply(x));
            first = false;
        }
        sb.append(end);
        return sb.toString();
    }

    private static <T> void feedString(List<T> xs, Consumer<T> proc, Consumer<String> writer, String start, String sep, String end) {
        writer.accept(start);
        var first = true;
        for (var x : xs) {
            if (!first) {
                writer.accept(sep);
            }
            proc.accept(x);
            first = false;
        }
        writer.accept(end);
    }

    public PrettyPrinter(Consumer<String> w, int indentWidth) {
        this.w = w;
        this.indentWidth = indentWidth;
    }

    public void programPrinter(Program p, int indent) {
        feedString(p.classDecls, c -> classPrinter(c, indent), w, "", "\n", "");
    }

    public void classPrinter(ClassDecl c, int indent) {
        w.accept(tab(indent));
        w.accept(String.format("class %s {\n", c.name));
        c.vars.forEach(vd -> {
            varDeclPrinter(vd, nextLevel(indent));
        });
        if (!c.vars.isEmpty()) {
            w.accept("\n");
        }
        feedString(c.mds, m -> mdDeclPrinter(m, nextLevel(indent)), w, "", "\n", "");
        w.accept(tab(indent) + "}\n");
    }

    public void varDeclPrinter(VarDecl vd, int indent) {
        w.accept(tab(indent) + vd.type + " " + vd.name + ";\n");
    }

    public void mdDeclPrinter(MdDecl md, int indent) {
        w.accept(mkString(
                md.params,
                param -> param.type + " " + param.name,
                tab(indent) + md.rtype + " " + md.name +"(",
                ", ",
                ") {\n"));
        md.vars.forEach(vd -> varDeclPrinter(vd, nextLevel(indent)));
        md.stmts.forEach(st -> stmtPrinter(st, nextLevel(indent)));
        w.accept(tab(indent) + "}\n");
    }

    public void stmtPrinter(Stmt st, int indent) {
        if (st instanceof Stmt.IfStmt) {
            var s = (Stmt.IfStmt) st;
            w.accept(tab(indent) + "if (");
            exprPrinter(s.cond);
            w.accept(") {\n");
            s.then.forEach(s1 -> stmtPrinter(s1, nextLevel(indent)));
            w.accept(tab(indent) + "} else {\n");
            s.alt.forEach(s1 -> stmtPrinter(s1, nextLevel(indent)));
            w.accept(tab(indent) + "}\n");
        } else if (st instanceof Stmt.WhileStmt) {
            var s = (Stmt.WhileStmt) st;
            w.accept(tab(indent) + "while (");
            exprPrinter(s.cond);
            w.accept(") {\n");
            s.block.forEach(s1 -> stmtPrinter(s1, nextLevel(indent)));
            w.accept(tab(indent) + "}\n");
        } else if (st instanceof Stmt.FnCall) {
            var s = (Stmt.FnCall) st;
            w.accept(tab(indent));
            exprPrinter(s.fn);
            feedString(s.args, this::exprPrinter, w, "(", ", ", ");\n");
        } else if (st instanceof Stmt.Assignment) {
            var s = (Stmt.Assignment) st;
            w.accept(tab(indent));
            exprPrinter(((Stmt.Assignment) st).lvalue);
            w.accept(" " + Operator.ASSIGN.symbol + " ");
            exprPrinter(((Stmt.Assignment) st).rvalue);
            w.accept(";\n");
        } else if (st instanceof Stmt.Return) {
            var s = (Stmt.Return) st;
            w.accept(tab(indent) + "return ");
            exprPrinter(s.expr);
            w.accept(";\n");
        } else if (st instanceof Stmt.ReturnVoid) {
            var s = (Stmt.ReturnVoid) st;
            w.accept(tab(indent) + "return;\n");
        }
    }

    public void exprPrinter(Expr expr) {
        if (expr instanceof Expr.BoolLit) {
            var e = (Expr.BoolLit) expr;
            w.accept(String.valueOf(e.value));
        } else if (expr instanceof Expr.IntLit) {
            var e = (Expr.IntLit) expr;
            w.accept(String.valueOf(e.value));
        } else if (expr instanceof Expr.StringLit) {
            var e = (Expr.StringLit) expr;
            w.accept(String.valueOf(e.value));
        } else if (expr instanceof Expr.BinOp) {
            var e = (Expr.BinOp) expr;
            w.accept("(");
            exprPrinter(e.left);
            w.accept(" " + e.op.symbol + " ");
            exprPrinter(e.right);
            w.accept(")");
        } else if (expr instanceof Expr.UnOp) {
            var e = (Expr.UnOp) expr;
            w.accept(e.op.symbol);
            exprPrinter(e.expr);
        } else if (expr instanceof Expr.IdExpr) {
            var e = (Expr.IdExpr) expr;
            w.accept(e.id.toString());
        } else if (expr instanceof Expr.NewExpr) {
            var e = (Expr.NewExpr) expr;
            w.accept("new " + e.className);
        } else if (expr instanceof Expr.ThisExpr) {
            w.accept("this");
        } else if (expr instanceof Expr.Null) {
            w.accept("null");
        } else if (expr instanceof Expr.FnCall) {
            var e = (Expr.FnCall) expr;
            exprPrinter(e.fn);
            feedString(e.args, this::exprPrinter, w, "(", ", ", ")");
        } else if (expr instanceof Expr.Path) {
            var e = (Expr.Path) expr;
            exprPrinter(e.left);
            w.accept("." + e.id);
        }
    }
}
