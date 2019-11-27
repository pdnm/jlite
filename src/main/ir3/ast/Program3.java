package ir3.ast;

import ast.Program;
import utils.ListUtils;

import java.util.List;

public class Program3 implements Ir3Node {
    public final List<CData3> classes;
    public final List<CMtd3> methods;

    public Program3(List<CData3> classes, List<CMtd3> methods) {
        this.classes = classes;
        this.methods = methods;
    }

    public static Program3 concat(Program3 p1, Program3 p2) {
        return new Program3(
                ListUtils.concat(p1.classes, p2.classes),
                ListUtils.concat(p1.methods, p2.methods));
    }

    @Override
    public void display(StringBuilder sb, int indent) {
        for (var cls : classes) {
            cls.display(sb, indent);
            sb.append("\n");
        }
        for (var method : methods) {
            method.display(sb, indent);
            sb.append("\n");
        }
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        display(sb, 0);
        return sb.toString();
    }
}
