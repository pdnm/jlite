package ir3;

import ast.*;
import ir3.ast.*;
import type_check.ClassDesc;
import type_check.CompileErrorExp;
import type_check.Context;
import type_check.MdType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Translator {
    public Program3 translate(Program program) {
        try {
            var classDesc = ClassDesc.initialize(program);
            Program3 result = null;
            for (var classDecl : program.classDecls) {
                var ctx = new Context3(new Context(classDesc, classDecl.name));
                var translatedClass = translate(ctx, classDecl);
                if (result == null) result = translatedClass;
                else result = Program3.concat(result, translatedClass);
            }
            return result;
        } catch (CompileErrorExp e) {
            throw new IllegalArgumentException("Input program is not type-checked");
        }
    }

    Program3 translate(Context3 ctx, ClassDecl cls) {
        var cdata = new CData3(Type3.fromType(cls.name),
                cls.vars.stream().map(VarDecl3::fromVarDecl).collect(Collectors.toList()));
        var methods = cls.mds.stream().map(md -> translate(ctx, cdata, md)).collect(Collectors.toList());
        return new Program3(List.of(cdata), methods);
    }

    CMtd3 translate(Context3 ctx, CData3 cdata, MdDecl md) {
        var lctx = new LocalContext();
        var vars = md.vars.stream().map(VarDecl3::fromVarDecl).collect(Collectors.toList());
        var stmt3s = new ArrayList<Stmt3>();

        for (var stmt : md.stmts) {
            var res = translate(ctx, lctx, stmt);
            vars.addAll(res.tmps);
            stmt3s.addAll(res.stmt3s);
        }

        var params = new ArrayList<FmlParam3>();
        params.add(new FmlParam3(cdata.cname, new Id3("this")));
        params.addAll(FmlParam3.fromFmlParams(md.params));
        var body = new MdBody3(vars, stmt3s);
        return new CMtd3(
                Type3.fromType(md.rtype),
                ctx.getId3LocalFunction(md.name, new MdType(md).argTypes).get(),
                params,
                body);
    }

    TranslatedStmt translate(Context3 ctx, LocalContext lctx, List<Stmt> inputStmts) {
        var tmps = new ArrayList<VarDecl3>();
        var stmts = new ArrayList<Stmt3>();

        for (var stmt : inputStmts) {
            var stmtCode = translate(ctx, lctx, stmt);
            tmps.addAll(stmtCode.tmps);
            stmts.addAll(stmtCode.stmt3s);
        }
        return new TranslatedStmt(tmps, stmts);
    }

    TranslatedExpr translateFnCall(Context3 ctx, LocalContext lctx, Expr func, List<Expr> fnArgs) {
        var tmps = new ArrayList<VarDecl3>();
        var stmts = new ArrayList<Stmt3>();

        var args = new ArrayList<Id3>();
        var argTypes = fnArgs.stream().map(Expr::getType).collect(Collectors.toList());
        Id3 fn;

        if (func instanceof Expr.IdExpr) {
            var localMethod = ctx.getId3LocalFunction(((Expr.IdExpr) func).id, argTypes);
            if (localMethod.isPresent()) {
                fn = localMethod.get();
                args.add(Context3.thisId);
            } else {
                fn = ctx.getId3BuiltinFunction(((Expr.IdExpr) func).id, argTypes).get();
            }
        } else {
            Expr.Path path = (Expr.Path) func;
            fn = ctx.getId3Function(path.left.getType(), path.id, argTypes).get();
            var obj = translate(ctx, lctx, path.left);
            tmps.addAll(obj.tmps);
            stmts.addAll(obj.stmt3s);
            var objVar = lctx.generateTmpVar();
            tmps.add(new VarDecl3(Type3.fromType(path.left.getType()), objVar));
            stmts.add(new Stmt3.Asn(objVar, obj.finalExpr));
            args.add(objVar);
        }

        for (var arg : fnArgs) {
            var argCode = translate(ctx, lctx, arg);
            tmps.addAll(argCode.tmps);
            stmts.addAll(argCode.stmt3s);
            var argVar = lctx.generateTmpVar();
            tmps.add(new VarDecl3(Type3.fromType(arg.getType()), argVar));
            stmts.add(new Stmt3.Asn(argVar, argCode.finalExpr));
            args.add(argVar);
        }

        return new TranslatedExpr(tmps, stmts, new Expr3.FnCall(fn, args));
    }

    TranslatedStmt translate(Context3 ctx, LocalContext lctx, Stmt stmt) {
        var tmps = new ArrayList<VarDecl3>();
        var stmts = new ArrayList<Stmt3>();

        var result = stmt.process(
                ifStmt -> {
                    var condValue = translate(ctx, lctx, ifStmt.cond);
                    var condVar = lctx.generateTmpVar();
                    tmps.addAll(condValue.tmps);
                    stmts.addAll(condValue.stmt3s);
                    tmps.add(new VarDecl3(Context3.boolType, condVar));
                    stmts.add(new Stmt3.Asn(condVar, condValue.finalExpr));

                    var l1 = lctx.generateLabel();
                    var l2 = lctx.generateLabel();
                    stmts.add(new Stmt3.If(condVar, l1));

                    var thenBlock = translate(ctx, lctx, ifStmt.then);
                    var elseBlock = translate(ctx, lctx, ifStmt.alt);
                    tmps.addAll(thenBlock.tmps);
                    tmps.addAll(elseBlock.tmps);
                    stmts.addAll(elseBlock.stmt3s);
                    stmts.add(new Stmt3.Goto(l2));
                    stmts.add(l1);
                    stmts.addAll(thenBlock.stmt3s);
                    stmts.add(l2);
                    return true;
                } ,
                whileStmt -> {
                    var l1 = lctx.generateLabel();
                    var l2 = lctx.generateLabel();
                    var l3 = lctx.generateLabel();
                    var condValue = translate(ctx, lctx, whileStmt.cond);
                    var condVar = lctx.generateTmpVar();
                    tmps.addAll(condValue.tmps);
                    tmps.add(new VarDecl3(Context3.boolType, condVar));
                    stmts.add(l1);
                    stmts.addAll(condValue.stmt3s);
                    stmts.add(new Stmt3.Asn(condVar, condValue.finalExpr));
                    stmts.add(new Stmt3.If(condVar, l2));
                    stmts.add(new Stmt3.Goto(l3));
                    stmts.add(l2);

                    var body = translate(ctx, lctx, whileStmt.block);
                    tmps.addAll(body.tmps);
                    stmts.addAll(body.stmt3s);
                    stmts.add(new Stmt3.Goto(l1));
                    stmts.add(l3);

                    return true;
                },
                fnCall -> {
                    var callCode = translateFnCall(ctx, lctx, fnCall.fn, fnCall.args);
                    var fnExpr = (Expr3.FnCall) callCode.finalExpr;
                    tmps.addAll(callCode.tmps);
                    stmts.addAll(callCode.stmt3s);
                    stmts.add(new Stmt3.FnCall(fnExpr.fn, fnExpr.args));
                    return true;
                },
                assignment -> {
                    var rvalueCode = translate(ctx, lctx, assignment.rvalue);

                    if (assignment.lvalue instanceof Expr.IdExpr) {
                        var id = ((Expr.IdExpr) assignment.lvalue).id;
                        tmps.addAll(rvalueCode.tmps);
                        stmts.addAll(rvalueCode.stmt3s);
                        if (id.isFromLocalScope()) {
                            stmts.add(new Stmt3.Asn(Id3.fromId(id), rvalueCode.finalExpr));
                        } else {
                            stmts.add(new Stmt3.FdAsn(Context3.thisId, Id3.fromId(id), rvalueCode.finalExpr));
                        }
                    } else {
                        Expr.Path path = (Expr.Path) assignment.lvalue;
                        var obj = translate(ctx, lctx, path.left);
                        tmps.addAll(obj.tmps);
                        stmts.addAll(obj.stmt3s);
                        tmps.addAll(rvalueCode.tmps);
                        stmts.addAll(rvalueCode.stmt3s);
                        var objVar = lctx.generateTmpVar();
                        tmps.add(new VarDecl3(Type3.fromType(path.left.getType()), objVar));
                        stmts.add(new Stmt3.Asn(objVar, obj.finalExpr));
                        stmts.add(new Stmt3.FdAsn(objVar, Id3.fromId(path.id), rvalueCode.finalExpr));
                    }
                    return true;
                },
                aReturn -> {
                    var valueCode = translate(ctx, lctx, aReturn.expr);
                    tmps.addAll(valueCode.tmps);
                    stmts.addAll(valueCode.stmt3s);
                    var tmpVar = lctx.generateTmpVar();
                    tmps.add(new VarDecl3(Type3.fromType(aReturn.expr.getType()), tmpVar));
                    stmts.add(new Stmt3.Asn(tmpVar, valueCode.finalExpr));
                    stmts.add(new Stmt3.Return(tmpVar));
                    return true;
                },
                returnVoid -> {
                    var tmpVar = lctx.generateTmpVar();
                    tmps.add(new VarDecl3(new Type3("Int"), tmpVar));
                    stmts.add(new Stmt3.Return(tmpVar));
                    return true;
                }
        );
        return new TranslatedStmt(tmps, stmts);
    }

    TranslatedExpr translate(Context3 ctx, LocalContext lctx, Expr expr) {
        var tmps = new ArrayList<VarDecl3>();
        var stmts = new ArrayList<Stmt3>();

        Expr3 finalExpr = expr.process(
                boolLit -> new Expr3.BoolConst(boolLit.value),
                intLit -> new Expr3.IntConst(intLit.value),
                stringLit -> new Expr3.StringConst(stringLit.value),
                binOp -> {
                    var leftCode = translate(ctx, lctx, binOp.left);
                    var rightCode = translate(ctx, lctx, binOp.right);
                    tmps.addAll(leftCode.tmps);
                    tmps.addAll(rightCode.tmps);
                    var leftVar = lctx.generateTmpVar();
                    tmps.add(new VarDecl3(Type3.fromType(binOp.left.getType()), leftVar));

                    stmts.addAll(leftCode.stmt3s);
                    stmts.add(new Stmt3.Asn(leftVar, leftCode.finalExpr));
                    if (binOp.op == Operator.AND || binOp.op == Operator.OR) { // Short-circuit
                        var cond = leftVar;
                        if (binOp.op == Operator.AND) {
                            var negate = lctx.generateTmpVar();
                            tmps.add(new VarDecl3(Type3.fromType(binOp.left.getType()), negate));
                            stmts.add(new Stmt3.Asn(negate, new Expr3.UnOp(Operator.NEG, leftVar)));
                            cond = negate;
                        }
                        var l1 = lctx.generateLabel();
                        stmts.add(new Stmt3.If(cond, l1));
                        stmts.addAll(rightCode.stmt3s);
                        stmts.add(new Stmt3.Asn(leftVar, rightCode.finalExpr));
                        stmts.add(l1);
                        return new Expr3.IdExpr(leftVar);
                    } else {
                        var rightVar = lctx.generateTmpVar();
                        tmps.add(new VarDecl3(Type3.fromType(binOp.right.getType()), rightVar));
                        stmts.addAll(rightCode.stmt3s);
                        stmts.add(new Stmt3.Asn(rightVar, rightCode.finalExpr));
                        return new Expr3.BinOp(binOp.op, leftVar, rightVar);
                    }
                },
                unOp -> {
                    var operandCode = translate(ctx, lctx, unOp.expr);
                    tmps.addAll(operandCode.tmps);
                    stmts.addAll(operandCode.stmt3s);
                    var operandVar = lctx.generateTmpVar();
                    tmps.add(new VarDecl3(Type3.fromType(unOp.expr.getType()), operandVar));
                    stmts.add(new Stmt3.Asn(operandVar, operandCode.finalExpr));
                    return new Expr3.UnOp(unOp.op, operandVar);
                },
                idExpr -> {
                    if (idExpr.id.isFromLocalScope()) {
                        return new Expr3.IdExpr(Id3.fromId(idExpr.id));
                    } else {
                        return new Expr3.FieldAccess(Context3.thisId, Id3.fromId(idExpr.id));
                    }
                },
                newExpr -> new Expr3.NewExpr(Type3.fromType(newExpr.className)),
                thisExpr -> new Expr3.IdExpr(Context3.thisId),
                aNull -> new Expr3.Null(),
                fnCall -> {
                    var callCode = translateFnCall(ctx, lctx, fnCall.fn, fnCall.args);
                    var fnExpr = (Expr3.FnCall) callCode.finalExpr;
                    tmps.addAll(callCode.tmps);
                    stmts.addAll(callCode.stmt3s);
                    return new Expr3.FnCall(fnExpr.fn, fnExpr.args);
                },
                path -> {
                    var leftCode = translate(ctx, lctx, path.left);
                    var tmpVar = lctx.generateTmpVar();
                    tmps.addAll(leftCode.tmps);
                    stmts.addAll(leftCode.stmt3s);
                    tmps.add(new VarDecl3(Type3.fromType(path.left.getType()), tmpVar));
                    stmts.add(new Stmt3.Asn(tmpVar, leftCode.finalExpr));
                    return new Expr3.FieldAccess(tmpVar, Id3.fromId(path.id));
                }
        );
        return new TranslatedExpr(tmps, stmts, finalExpr);
    }

    static private class LocalContext {
        int tmpVarId = 0;
        int labelId = 0;

        Id3 generateTmpVar() {
            var id = new Id3("_tmp" + tmpVarId);
            tmpVarId++;
            return id;
        }

        Stmt3.Label generateLabel() {
            var label = new Stmt3.Label(labelId);
            labelId++;
            return label;
        }
    }

    static private class TranslatedStmt {
        final List<VarDecl3> tmps;
        final List<Stmt3> stmt3s;

        TranslatedStmt(List<VarDecl3> tmps, List<Stmt3> stmt3s) {
            this.tmps = tmps;
            this.stmt3s = stmt3s;
        }
    }

    static private class TranslatedExpr {
        final List<VarDecl3> tmps;
        final List<Stmt3> stmt3s;
        final Expr3 finalExpr;

        TranslatedExpr(List<VarDecl3> tmps, List<Stmt3> stmt3s, Expr3 finalExpr) {
            this.tmps = tmps;
            this.stmt3s = stmt3s;
            this.finalExpr = finalExpr;
        }
    }

    private static TranslatedExpr simpleExpr(Expr3 e) {
        return new TranslatedExpr(List.of(), List.of(), e);
    }
}
