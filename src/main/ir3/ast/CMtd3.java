package ir3.ast;

import java.util.List;
import java.util.stream.Collectors;

public class CMtd3 implements Ir3Node {
    final Type3 rtype;
    final Id3 name;
    final List<FmlParam3> params;
    final MdBody3 body;

    public CMtd3(Type3 rtype, Id3 name, List<FmlParam3> params, MdBody3 body) {
        this.rtype = rtype;
        this.name = name;
        this.params = params;
        this.body = body;
    }

    @Override
    public void display(StringBuilder sb, int indent) {
        sb.append(Ir3Node.tab(indent))
                .append(rtype.name).append(" ").append(name.name).append("(")
                .append(params.stream().map(FmlParam3::toString).collect(Collectors.joining(", ")))
                .append(") {\n");
        body.display(sb, indent + ts);
        sb.append(" ".repeat(indent) + "}\n");
    }
}
