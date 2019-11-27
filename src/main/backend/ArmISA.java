package backend;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ArmISA {
    public interface Instr {}

    public static class Register {
        int id;
        String name;

        public Register(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static abstract class Address {
        public static class Label extends Address {
            String name;

            public Label(String name) {
                this.name = name;
            }

            @Override
            public String toString() {
                return "=" + name;
            }
        }

        public static class PreIndexed extends Address {
            Register base;
            int offset;
            static int offsetLength = 12;

            public PreIndexed(Register base, int offset) {
                this.base = base;
                this.offset = offset;
            }

            @Override
            public String toString() {
                return "[" + base + ", #" + offset + "]";
            }
        }
    }

    public static abstract class Operand2 {
        public static class Immed extends Operand2 {
            int value;
            static int immLength = 8;

            public Immed(int value) {
                this.value = value;
            }

            @Override
            public String toString() {
                return "#" + value;
            }
        }

        public static class RegOp extends Operand2 {
            Register register;

            public RegOp(Register register) {
                assert(register != null);
                this.register = register;
            }

            @Override
            public String toString() {
                return register.toString();
            }
        }
    }

    public static class DataInstr implements Instr {
        String op;
        Register rd;
        Register rn;
        Operand2 operand2;

        public DataInstr(String op, Register rd, Register rn, Operand2 operand2) {
            this.op = op;
            this.rd = rd;
            this.rn = rn;
            this.operand2 = operand2;
        }

        @Override
        public String toString() {
            return op + " " + rd + ", " + rn + ", " + operand2;
        }
    }

    public static DataInstr add(Register rd, Register rn, Operand2 operand2) {
        return new DataInstr("add", rd, rn, operand2);
    }

    public static DataInstr sub(Register rd, Register rn, Operand2 operand2) {
        return new DataInstr("sub", rd, rn, operand2);
    }

    public static DataInstr and(Register rd, Register rn, Operand2 operand2) {
        return new DataInstr("and", rd, rn, operand2);
    }
    public static DataInstr orr(Register rd, Register rn, Operand2 operand2) {
        return new DataInstr("orr", rd, rn, operand2);
    }

    public static DataInstr mul(Register rd, Register rn, Register rm) {
        return new DataInstr("mul", rd, rn, new Operand2.RegOp(rm));
    }

    public static class CmpInstr implements Instr {
        Register rn;
        Register rm;

        public CmpInstr(Register rn, Register rm) {
            this.rn = rn;
            this.rm = rm;
        }

        @Override
        public String toString() {
            return "cmp " + rn + ", " + rm;
        }
    }

    public static class Branch implements Instr {
        String cond;
        String location;

        public Branch(String cond, String location) {
            this.cond = cond;
            this.location = location;
        }

        @Override
        public String toString() {
            return "b" + cond + "\t" + location;
        }
    }

    public static Branch beq(String location) {
        return new Branch("eq", location);
    }

    public static class BLink implements Instr {
        String location;

        public BLink(String location) {
            this.location = location;
        }

        @Override
        public String toString() {
            return "bl " + location;
        }
    }

    public static class MemInstr implements Instr {
        String op;
        Register rd;
        Address address;

        public MemInstr(String op, Register rd, Address address) {
            this.op = op;
            this.rd = rd;
            this.address = address;
        }

        @Override
        public String toString() {
            return op + " " + rd + ", " + address;
        }
    }

    public static Instr load(Register rd, Address address) {
        return new MemInstr("ldr", rd, address);
    }
    public static Instr store(Register rd, Address address) {
        return new MemInstr("str", rd, address);
    }

    public static class MoveInstr implements Instr {
        Register rd;
        Operand2 operand2;
        String cond;
        static int valueLength = 12;

        public MoveInstr(Register rd, int value) {
            this.rd = rd;
            this.operand2 = new Operand2.Immed(value);
            this.cond = "";
        }

        public MoveInstr(Register rd, int value, String cond) {
            this.rd = rd;
            this.operand2 = new Operand2.Immed(value);
            this.cond = cond;
        }

        public MoveInstr(Register rd, Register r) {
            this.rd = rd;
            this.operand2 = new Operand2.RegOp(r);
            this.cond = "";
        }

        @Override
        public String toString() {
            return "mov" + cond + " " + rd + ", " + operand2;
        }
    }

    public static class Label implements Instr {
        String label;

        public Label(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label + ":";
        }
    }

    public static class Push implements Instr {
        public List<Register> registers;

        public Push(List<Register> registers) {
            this.registers = new ArrayList<>(registers);
            this.registers.sort(new Comparator<Register>() {
                @Override
                public int compare(Register o1, Register o2) {
                    return Integer.compare(o1.id, o2.id);
                }
            });
        }

        @Override
        public String toString() {
            return "push {" +
                    registers.stream()
                            .map(Register::toString)
                            .collect(Collectors.joining(", "))
                    + "}";
        }
    }

    public static class Pop implements Instr {
        public List<Register> registers;

        public Pop(List<Register> registers) {
            this.registers = new ArrayList<>(registers);
            this.registers.sort(new Comparator<Register>() {
                @Override
                public int compare(Register o1, Register o2) {
                    return Integer.compare(o1.id, o2.id);
                }
            });
        }

        @Override
        public String toString() {
            return "pop {" +
                    registers.stream()
                            .map(Register::toString)
                            .collect(Collectors.joining(", "))
                    + "}";
        }
    }
    public static class Directive implements Instr {
        String directive;

        public Directive(String directive) {
            this.directive = directive;
        }

        @Override
        public String toString() {
            return directive;
        }
    }
}
