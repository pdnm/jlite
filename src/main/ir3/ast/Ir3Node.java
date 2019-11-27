package ir3.ast;

public interface Ir3Node {
    static int ts = 4;
    static String tab(int indent) {
        return " ".repeat(indent);
    }
    default void display(StringBuilder sb, int indent) {
        sb.append(tab(indent) + this.toString() + "\n");
    }
}
