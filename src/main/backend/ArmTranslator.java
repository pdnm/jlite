package backend;

import static ast.Operator.*;
import ir3.ast.*;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static backend.ArmISA.*;

public class ArmTranslator {
    private final int intSize;
    private final int numArgRegs;
    private final int numVarRegs;
    private int nLabels = 0;
    private Register[] argRegs;
    private Register[] varRegs;
    private Register[] allocRegs; // argRegs ++ varRegs;
    private Register tmpReg;
    private Register fp;
    private Register sp;
    private Register lr;
    private Register pc;

    private Map<Type3, CData3> classes;
    private int getClassSize(CData3 cls) {
        return cls.fields.size() * intSize;
    }
    private Type3 getType(CMtd3 method, Id3 var) {
        Type3 type;
        for (var param : method.params) if (param.name.equals(var))
            return param.type;
        for (var decl : method.body.vars) if (decl.name.equals(var))
            return decl.type;
        throw new RuntimeException("Unreachable");
    }
    private int getFieldOffset(CData3 cls, Id3 field) {
        for (int i = 0; i < cls.fields.size(); i++) if (cls.fields.get(i).name.equals(field))
            return i * intSize;
        throw new RuntimeException("Field " + field + " is not in class " + cls.cname);
    }

    Map<String, String> pervasives = Map.of(
            "%Pervasive_printf", "readln"
    );
    private String getFunctionName(String rawName) {
        return rawName.replace("%", ".");
    }

    private String generateLabel() {
        var res = ".L" + nLabels;
        nLabels++;
        return res;
    }

    private String generateConstLabel() {
        var res = ".LC" + nLabels;
        nLabels++;
        return res;
    }

    public ArmTranslator(int intSize, int numArgRegs, int numVarRegs, int tmpReg, int fp, int sp, int lr, int pc) {
        this.intSize = intSize;
        this.numArgRegs = numArgRegs;
        this.numVarRegs = numVarRegs;
        this.classes = new HashMap<>();
        argRegs = new Register[numArgRegs];
        varRegs = new Register[numVarRegs];
        allocRegs = new Register[numArgRegs + numVarRegs];
        for (int i = 0; i < numArgRegs; i++) {
            argRegs[i] = new Register(i, "a" + (i + 1));
            allocRegs[i] = argRegs[i];
        }
        for (int i = 0; i < numVarRegs; i++) {
            varRegs[i] = new Register(i + numArgRegs, "r" + (i + numArgRegs));
            allocRegs[i + numArgRegs] = varRegs[i];
        }
        this.tmpReg = new Register(tmpReg, "r" + tmpReg);
        this.fp = new Register(fp, "fp");
        this.sp = new Register(sp, "sp");
        this.lr = new Register(lr, "lr");
        this.pc = new Register(pc, "pc");
    }


    public List<Instr> translate(Program3 program) {
        classes.clear();
        for (var cls : program.classes)
            classes.put(cls.cname, cls);

        ArrayList<Instr> instructions = new ArrayList<>();
        for (var fn : program.methods) {
            instructions.addAll(translate(fn));
        }
        return instructions;
    }

    private List<Instr> translate(CMtd3 method) {
        List<Instr> instructions = new ArrayList<>();
        String mdName = getFunctionName(method.name.name);
        instructions.add(new Directive(".text"));
        instructions.add(new Directive(".global " + mdName));
        instructions.add(new Directive(".type " + mdName + ", %function"));
        instructions.add(new Label(mdName));

        List<Register> calleeSaved = new ArrayList<>(Arrays.asList(varRegs));
        calleeSaved.add(fp);
        calleeSaved.add(lr);
        instructions.add(new Push(calleeSaved));
        instructions.add(sub(fp, sp, new Operand2.Immed(intSize)));
//        instructions.add(new MoveInstr(fp, sp));
        String epilog = generateLabel();

        var liveness = Liveness.analyze(new ArrayList<>(method.body.stmts));
        Map<Id3, Integer> allocReg = new RegisterAllocator(allocRegs.length).allocate(method, liveness);
        Map<Id3, Register> registerOf = allocReg.entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), allocRegs[entry.getValue()]))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<Id3, Integer> fpOffset = new HashMap<>();
        ArrayList<Id3> vars = method.allVars();
        List<Id3> params = method.params.stream().map(p -> p.name).collect(Collectors.toList());

        Function<Id3, Address> getSpilledAddr = var -> new Address.PreIndexed(fp, fpOffset.get(var) * intSize);

        for (var var : vars) {
            if (!allocReg.containsKey(var)) {
                int os = -fpOffset.size(); // [fp] + os * intSize
                fpOffset.put(var, os);

                int pos = params.indexOf(var);
                if (pos != -1) {
                    if (pos < numArgRegs) { // arg in a register
                        instructions.add(store(argRegs[pos], getSpilledAddr.apply(var)));
                    } else {
                        int argOffset = calleeSaved.size() + (pos - numArgRegs + 1); //TODO
                        instructions.add(load(tmpReg, new Address.PreIndexed(fp, argOffset * intSize)));
                    }
                }
            }
        }
        instructions.add(sub(sp, sp, new Operand2.Immed(intSize * fpOffset.size())));

        BiConsumer<Integer, List<Register>> pushManually = (base, registers) -> {
            for (int i = 0; i < registers.size(); i++) {
                var reg = registers.get(i);
                instructions.add(new Push(List.of(reg)));
//                instructions.add(store(reg, new Address.PreIndexed(fp, -intSize * (base +  i))));
            }
//            instructions.add(sub(sp, sp, new Operand2.Immed(intSize * registers.size())));
        };
        BiConsumer<Integer, List<Register>> popManually = (base, registers) -> {
            for (int i = registers.size() - 1; i >= 0; i--) {
                var reg = registers.get(i);
                instructions.add(new Pop(List.of(reg)));
//                instructions.add(load(reg, new Address.PreIndexed(fp, -intSize * (base +  i))));
            }
//            instructions.add(add(sp, sp, new Operand2.Immed(intSize * registers.size())));
        };

        BiConsumer<List<Register>, List<Register>> moveRegisters = (from, to) -> {
            for (int i = 0; i < from.size(); i++) {
                var x = from.get(i);
                instructions.add(new Push(List.of(x)));
            }
            for (int i = to.size() - 1; i >= 0; i--) {
                var y = to.get(i);
                instructions.add(new Pop(List.of(y)));
            }
        };

        /* Move non-spilled arguments to their allocated registers */{
            List<Register> toPush = new ArrayList<>();
            List<Register> toPop = new ArrayList<>();
            List<Instr> toLoads = new ArrayList<>();
            for (var var : vars) {
                if (allocReg.containsKey(var)) {
                    Register reg = registerOf.get(var);
                    int pos = params.indexOf(var);
                    if (pos != -1) {
                        if (pos < numArgRegs) { // arg in a register
                            toPush.add(argRegs[pos]);
                            toPop.add(reg);
                        } else {
                            int argOffset = calleeSaved.size() + (pos - numArgRegs + 1); //TODO
                            toLoads.add(load(reg, new Address.PreIndexed(fp, argOffset * intSize)));
                        }
                    }
                }
            }
//            moveRegisters.accept(toPush, toPop);
            pushManually.accept(fpOffset.size(), toPush);
            popManually.accept(fpOffset.size(), toPop);
            instructions.addAll(toLoads);
        }

        BiConsumer<Id3, List<Id3>> translateFnCall = (fn, args) -> {
            instructions.add(new Push(Arrays.asList(argRegs)));

            List<Register> toDump = new ArrayList<>();
            int stackTop = fpOffset.size() + numArgRegs;
            for (var var : args) {
                if (registerOf.containsKey(var)) {
                    fpOffset.put(var, -(stackTop + toDump.size()));
                    toDump.add(registerOf.get(var));
                }
            }
//            instructions.add(new Push(toDump));
            pushManually.accept(stackTop, toDump);

            for (int i = 0; i < args.size(); i++) {
                var var = args.get(i);
                if (i < numArgRegs) {
                    instructions.add(load(argRegs[i], getSpilledAddr.apply(var)));
                } else {
                    int offset = stackTop + toDump.size() + i - numArgRegs;
                    instructions.add(load(tmpReg, getSpilledAddr.apply(var)));
                    instructions.add(store(tmpReg, new Address.PreIndexed(fp, -offset*intSize)));
                }
            }
            instructions.add(new BLink(getFunctionName(fn.name)));
            instructions.add(new MoveInstr(tmpReg, argRegs[0]));

            if (args.size() > numArgRegs)
                instructions.add(sub(sp, sp, new Operand2.Immed(intSize * (args.size() - numArgRegs))));

            for (var var : args) {
                if (registerOf.containsKey(var)) {
                    fpOffset.remove(var);
                }
            }
            popManually.accept(stackTop, toDump);
            instructions.add(new Pop(Arrays.asList(argRegs)));
        };

        BiConsumer<Register, Expr3> translateExpr = (target, expr) -> {
            Map<Id3, Register> tmpRegisterOf = new HashMap<>();
            List<Register> toDump = new ArrayList<>();
            if (!(expr instanceof Expr3.FnCall)) {
                for (var var : expr.vars()) {
                    if (registerOf.containsKey(var))
                        tmpRegisterOf.put(var, registerOf.get(var));
                }
                for (var var : expr.vars()) { // dump some registers to load expr vars
                    if (!registerOf.containsKey(var)) {
                        for (var reg : allocRegs) if (!reg.equals(target) && !tmpRegisterOf.containsValue(reg)) {
                            toDump.add(reg);
                            tmpRegisterOf.put(var, reg);
                            break;
                        }
                    }
                }
//                instructions.add(new Push(toDump));
                pushManually.accept(fpOffset.size(), toDump);
                for (var var : expr.vars()) { // dump some registers to load expr vars
                    if (!registerOf.containsKey(var)) {
                        instructions.add(load(tmpRegisterOf.get(var), getSpilledAddr.apply(var)));
                    }
                }
            }
            Map<String, String> operatorName = Map.of(
                    "+", "add",
                    "-", "sub",
                    "&&", "and",
                    "||", "orr"
            );
            Map<String, String> cmpName = Map.of(
                    ">", "gt",
                    "<", "lt",
                    ">=", "ge",
                    "<=", "le",
                    "==", "eq",
                    "!=", "ne"
            );
            expr.process(
                    binOp -> {
                        if (operatorName.containsKey(binOp.op.symbol)) {
                            instructions.add(new DataInstr(operatorName.get(binOp.op.symbol), tmpReg, tmpRegisterOf.get(binOp.left),
                                    new Operand2.RegOp(tmpRegisterOf.get(binOp.right))));
                        } else if (binOp.op.equals(MUL)) {
                            instructions.add(mul(tmpReg, tmpRegisterOf.get(binOp.left),
                                    tmpRegisterOf.get(binOp.right)));
                        } else if (List.of(LT, GT, LE, GE, EQ, NEQ).contains(binOp.op)) {
                            instructions.add(new CmpInstr(tmpRegisterOf.get(binOp.left), tmpRegisterOf.get(binOp.right)));
                            instructions.add(new MoveInstr(tmpReg, 0));
                            instructions.add(new MoveInstr(tmpReg, 1, cmpName.get(binOp.op.symbol)));
                        } else {
                            throw new RuntimeException("Binary operator " + binOp.op + " not supported");
                        }
                        return true;
                    },
                    unOp -> {
                        var operandReg = tmpRegisterOf.get(unOp.operand);
                        switch (unOp.op) {
                            case UMINUS:
                                instructions.add(new MoveInstr(tmpReg, 0));
                                instructions.add(sub(tmpReg, tmpReg, new Operand2.RegOp(operandReg)));
                                break;
                            case NEG:
                                instructions.add(new DataInstr("eor", tmpReg, operandReg, new Operand2.Immed(1)));
                                break;
                            default:
                                throw new RuntimeException("Unary operator not supported");
                        }
                        return true;
                    },
                    fnCall -> {
                        if (fnCall.fn.name.startsWith("%Pervasive_isNull%")) { // object is null if the ref is 0
                            instructions.add(new MoveInstr(tmpReg, 0));
                            instructions.add(new CmpInstr(argRegs[0], tmpReg));
                            instructions.add(new MoveInstr(tmpReg, 0));
                            instructions.add(new MoveInstr(tmpReg, 1, "eq"));
                        } else translateFnCall.accept(fnCall.fn, fnCall.args);
                        return true;
                    },
                    fieldAccess -> {
                        Type3 type = getType(method, fieldAccess.obj);
                        CData3 cls = classes.get(type);
                        instructions.add(load(tmpReg, new Address.PreIndexed(
                                tmpRegisterOf.get(fieldAccess.obj),
                                getFieldOffset(cls, fieldAccess.field))));
                        return true;
                    },
                    idExpr -> {
                        var rvalue = tmpRegisterOf.get(idExpr.id);
                        instructions.add(new MoveInstr(tmpReg, rvalue));
                        return true;
                    },
                    newExpr -> {
                        CData3 cls = classes.get(newExpr.type);
                        instructions.add(new Push(Arrays.asList(argRegs)));
                        instructions.add(new MoveInstr(argRegs[0], getClassSize(cls)));
                        instructions.add(new BLink("_Znwj(PLT)"));

                        // Initialize all fields to 0
                        instructions.add(new MoveInstr(tmpReg, 0));
                        for (int i = 0; i < cls.fields.size(); i++) {
                            instructions.add(store(tmpReg, new Address.PreIndexed(argRegs[0], i)));
                        }

                        instructions.add(new MoveInstr(tmpReg, argRegs[0]));
                        instructions.add(new Pop(Arrays.asList(argRegs)));
                        return true;
                    },
                    boolConst -> {
                        instructions.add(new MoveInstr(tmpReg, boolConst.value ? 1 : 0));
                        return true;
                    },
                    intConst -> {
                        instructions.add(load(tmpReg, new Address.Label(String.valueOf(intConst.value))));
                        return true;
                    },
                    stringConst -> {
                        String lc = generateConstLabel();
                        instructions.add(new Label(lc));
                        instructions.add(new Directive(".asciz \"" + stringConst.value + "\""));
                        instructions.add(new Directive(".align"));
                        instructions.add(load(tmpReg, new Address.Label(lc)));
                        return true;
                    },
                    voidValue -> {
                        return true;
                    }
            );

            if (!(expr instanceof Expr3.FnCall)) {
                popManually.accept(fpOffset.size(), toDump);
            }
            instructions.add(new MoveInstr(target, tmpReg));
        };

        Map<Integer, String> irLabelToAsmLabel = new HashMap<>();
        for (var st : method.body.stmts) {
            if (st instanceof Stmt3.Label) {
                String lb = generateLabel();
                irLabelToAsmLabel.put(((Stmt3.Label) st).id, lb);
            }
        }

        for (var st : method.body.stmts) {
            st.process(
                    label -> {
                        instructions.add(new Label(irLabelToAsmLabel.get(label.id)));
                        return true;
                    },
                    anIf -> {
                        Register reg;
                        if (allocReg.containsKey(anIf.cond)) {
                            reg = registerOf.get(anIf.cond);
                        } else {
                            instructions.add(load(tmpReg, getSpilledAddr.apply(anIf.cond)));
                            reg = tmpReg;
                        }
                        instructions.add(new MoveInstr(lr, 1));
                        instructions.add(new CmpInstr(reg, lr));
                        instructions.add(beq(irLabelToAsmLabel.get(anIf.target.id)));
                        return true;
                    },
                    aGoto -> {
                        instructions.add(new Branch("", irLabelToAsmLabel.get(aGoto.target.id)));
                        return true;
                    },
                    asn -> {
                        Register target;
                        if (registerOf.containsKey(asn.lvalue)) {
                            translateExpr.accept(registerOf.get(asn.lvalue), asn.rvalue);
                        } else {
                            translateExpr.accept(tmpReg, asn.rvalue);
                            instructions.add(store(tmpReg, getSpilledAddr.apply(asn.lvalue)));
                        }
                        return true;
                    },
                    fdAsn -> {
                        Type3 type = getType(method, fdAsn.obj);
                        CData3 cls = classes.get(type);
                        Register objReg;
                        translateExpr.accept(tmpReg, fdAsn.rvalue);
                        if (registerOf.containsKey(fdAsn.obj)) {
                            objReg = registerOf.get(fdAsn.obj);
                        } else {
                            instructions.add(load(lr, getSpilledAddr.apply(fdAsn.obj)));
                            objReg = lr;
                        }
                        instructions.add(store(tmpReg,
                                new Address.PreIndexed( objReg, getFieldOffset(cls, fdAsn.field))));
                        return true;
                    },
                    fnCall -> {
                        if (fnCall.fn.name.startsWith("%Pervasive_println")) {
                            String lc = generateConstLabel();
                            if (registerOf.containsKey(fnCall.args.get(0))) {
                                var reg = registerOf.get(fnCall.args.get(0));
                                instructions.add(new MoveInstr(tmpReg, reg));
                            } else {
                                instructions.add(load(tmpReg, getSpilledAddr.apply(fnCall.args.get(0))));
                            }
                            instructions.add(new Push(Arrays.asList(argRegs)));
                            instructions.add(new Label(lc));

                            Type3 type = getType(method, fnCall.args.get(0));
                            if (type.name.equals("Int"))
                                instructions.add(new Directive(".asciz \"%d\\n\""));
                            else if (type.name.equals("String"))
                                instructions.add(new Directive(".asciz \"%s\\n\""));
                            else throw new RuntimeException("printf doesn't support type " + type);
                            instructions.add(new Directive(".align"));

                            instructions.add(load(argRegs[0], new Address.Label(lc)));
                            instructions.add(new MoveInstr(argRegs[1], tmpReg));
                            instructions.add(new BLink("printf(PLT)"));
                            instructions.add(new Pop(Arrays.asList(argRegs)));
                        } else translateFnCall.accept(fnCall.fn, fnCall.args);
                        return true;
                    },
                    aReturn -> {
                        if (registerOf.containsKey(aReturn.value)) {
                            instructions.add(new MoveInstr(argRegs[0], registerOf.get(aReturn.value)));
                        } else {
                            instructions.add(load(argRegs[0], getSpilledAddr.apply(aReturn.value)));
                        }
                        instructions.add(new Branch("", epilog));
                        return true;
                    }
            );
        }

        instructions.add(new Label(epilog));
        calleeSaved.set(calleeSaved.size() - 1, pc); // pc <- lr
        instructions.add(add(sp, fp, new Operand2.Immed(intSize)));
        instructions.add(new Pop(calleeSaved));
        return instructions;
    }
}
