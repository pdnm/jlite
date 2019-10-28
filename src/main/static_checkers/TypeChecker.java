package static_checkers;

import ast.*;
import utils.ListUtils;
import utils.Result;

import java.util.ArrayList;
import java.util.List;

import static utils.Result.*;
import static static_checkers.CompileError.*;

public class TypeChecker {
    public static List<CompileError> check(Program program) {
        try {
            var classDesc = ClassDesc.initialize(program);
            var errors = new ArrayList<CompileError>();
            for (var classDecl : program.classDecls) {
                var Context = new Context(classDesc, classDecl.name);
                errors.addAll(checkClass(Context, classDecl));
            }
            return errors;
        } catch (CompileErrorExp e) {
            return e.errors;
        }
    }

    private static List<CompileError> checkClass(Context ctx, ClassDecl classDecl) {
        var errors = new ArrayList<CompileError>();
        classDecl.vars.stream()
                .filter(varDecl -> !ctx.isLegitimate(varDecl.type))
                .map(varDecl -> typeNotDefined(varDecl.type))
                .forEach(errors::add);
        classDecl.mds.stream()
                .map(mdDecl -> checkMethod(ctx, mdDecl))
                .forEach(errors::addAll);
        return errors;
    }

    private static List<CompileError> checkMethod(Context ctx, MdDecl mdDecl) {
        var errors = new ArrayList<CompileError>();
        if (!ctx.isLegitimate(mdDecl.rtype))
            errors.add(typeNotDefined(mdDecl.rtype));
        mdDecl.params.stream()
                .filter(fmlParam -> !ctx.isLegitimate(fmlParam.type))
                .map(fmlParam -> typeNotDefined(fmlParam.type))
                .forEach(errors::add);
        mdDecl.vars.stream()
                .filter(varDecl -> !ctx.isLegitimate(varDecl.type))
                .map(varDecl -> typeNotDefined(varDecl.type))
                .forEach(errors::add);

        if (ListUtils.isNotUnique(mdDecl.vars, varDecl -> varDecl.type.name))
            errors.add(duplicatedVarNames(mdDecl));

        if (!errors.isEmpty()) return errors;

        var augmentedContext = ctx;
        for (var param : mdDecl.params)
            augmentedContext = augmentedContext.extend(param.name, param.type);
        for (var varDecl : mdDecl.vars)
            augmentedContext = augmentedContext.extend(varDecl.name, varDecl.type);

        return checkBlock(augmentedContext, mdDecl.stmts)
                .flatMap(
                        rtype -> rtype.equals(mdDecl.rtype)
                                ? success(rtype)
                                : singleError(retTypeMismatch(mdDecl, mdDecl.rtype, rtype)))
                .fold(x -> List.of(), xs -> xs);
    }

    private static Result<Type, List<CompileError>> checkBlock(Context ctx, List<Stmt> block) {
        var types = new ArrayList<Type>();
        var errors = new ArrayList<CompileError>();
        for (var stmt : block) {
            checkStatement(ctx, stmt).consume(types::add, errors::addAll);
        }
        if (errors.isEmpty()) return success(types.isEmpty() ? Context.voidType : types.get(types.size() - 1));
        else return error(errors);
    }

    static Result<Type, List<CompileError>> checkStatement(Context ctx, Stmt st) {
        return st.process(
                ifStmt ->
                        checkExpression(ctx, ifStmt.cond).flatMap(type -> {
                            if (!type.equals(Context.boolType))
                                return singleError(nonBooleanCondition(ifStmt.cond));
                            var r1 = checkBlock(ctx, ifStmt.then);
                            var r2 = checkBlock(ctx, ifStmt.alt);
                            return flatten(map2(r1, r2, (t1, t2) ->
                                    t1.equals(t2) ? success(t1) : singleError(branchTypeMismatch(ifStmt))));
                        })
                ,
                whileStmt ->
                        checkExpression(ctx, whileStmt.cond).flatMap(type -> {
                            if (type.equals(Context.boolType))
                                return singleError(nonBooleanCondition(whileStmt.cond));
                            return checkBlock(ctx, whileStmt.block);
                        })
                ,
                fnCall -> traverseAll(fnCall.args, arg -> checkExpression(ctx, arg), ListUtils::concat)
                        .flatMap(argTypes ->
                                getMethod(ctx, fnCall, fnCall.fn, argTypes)),
                assignment -> {
                    if (!isLvalue(assignment.lvalue))
                        return singleError(notLvalue(assignment.lvalue));
                    var r1 = checkExpression(ctx, assignment.lvalue);
                    var r2 = checkExpression(ctx, assignment.rvalue);
                    return flatten(map2(r1, r2, (t1, t2) ->
                            t1.equals(t2) ? success(Context.voidType) : singleError(assMismatch(assignment, t1, t2))));
                },
                aReturn -> checkExpression(ctx, aReturn.expr),
                returnVoid -> success(Context.voidType)
        );
    }

    static boolean isLvalue(Expr lvalue) {
        return lvalue instanceof Expr.IdExpr || lvalue instanceof Expr.Path;
    }

    static Result<Type, List<CompileError>> getMethod(Context ctx, AstNode call, Expr method, List<Type> argTypes) {
        if (method instanceof Expr.IdExpr) {
            Identifier md = ((Expr.IdExpr) method).id;
            return ctx.localClass.getMethodReturnType(md, argTypes)
                    .or(() -> ctx.pervasive.getMethodReturnType(md, argTypes))
                    .map(TypeChecker::ok)
                    .orElseGet(() -> singleError(methodNotDefined(call)));
        } else if (method instanceof Expr.Path) {
            var path = (Expr.Path) method;
            return checkExpression(ctx, path.left).flatMap(t ->
                    ctx.classDesc.getClassEnv(t)
                        .flatMap(cls -> cls.getMethodReturnType(path.id, argTypes))
                        .map((TypeChecker::ok))
                        .orElseGet(() -> singleError(methodNotDefined(call))));
        } else return singleError(notAMethod(method));
    }

    private static Result<Type, List<CompileError>> checkExpression(Context ctx, Expr expr) {
        Result<Type, List<CompileError>> result = expr.process(
                boolLit -> success(Context.boolType),
                intLit -> success(Context.intType),
                stringLit -> success(Context.stringType),
                binOp -> {
                    var opType = Context.binOps.get(binOp.op);
                    return checkExpression(ctx, binOp.left).flatMap(leftT ->
                            checkExpression(ctx, binOp.right).flatMap(rightT ->
                                    leftT.equals(opType.left) && rightT.equals(opType.right)
                                            ? success(opType.result)
                                            : singleError(binOpTypesMismatch(
                                                    expr, binOp.op, opType.left, opType.right, leftT, rightT))));
                },
                unOp -> {
                    var opType = Context.unOps.get(unOp.op);

                    return checkExpression(ctx, unOp.expr).flatMap(operandType ->
                                    operandType.equals(opType.operand)
                                            ? success(opType.result)
                                            : singleError(unOpTypeMismatch(
                                            expr, unOp.op, opType.operand, operandType)));
                },
                idExpr -> {
                    idExpr.id.setFromLocalScope(ctx.isLocalVariable(idExpr.id));
                    return ctx.getType(idExpr.id).map(TypeChecker::ok).orElseGet(() -> singleError(identifierNotDef(idExpr.id)));
                },
                newExpr -> ctx.isClass(newExpr.className) ? success(newExpr.className) : singleError(notClass(newExpr.className)),
                thisExpr -> success(ctx.localClass.name),
                aNull -> singleError(noNull(expr)),
                fnCall -> traverseAll(fnCall.args, arg -> checkExpression(ctx, arg), ListUtils::concat)
                        .flatMap(argTypes ->
                                getMethod(ctx, fnCall, fnCall.fn, argTypes)),
                path ->
                    checkExpression(ctx, path.left).flatMap(t ->
                            ctx.classDesc.getClassEnv(t)
                                    .flatMap(cls -> cls.getFieldType(path.id))
                                    .map(TypeChecker::ok)
                                    .orElseGet(() -> singleError(fieldNotDefined(path.id, t))))
        );
        result.consume(expr::setType, x -> {});
        return result;
    }

    private static <T> Result<T, List<CompileError>> singleError(CompileError e) {
        return singleError(e);
    }

    private static Result<Type, List<CompileError>> ok(Type type) {
        return success(type);
    }
}
