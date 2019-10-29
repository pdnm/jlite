package ir3.ast;

import java.util.List;

public class CData3 implements Ir3Node {
    final Type3 cname;
    final List<VarDecl3> fields;

    public CData3(Type3 cname, List<VarDecl3> fields) {
        this.cname = cname;
        this.fields = fields;
    }

    @Override
    public void display(StringBuilder sb, int indent) {
        sb.append("class ").append(cname.name).append("{\n");

        for (var fd : fields) {
            fd.display(sb, indent + ts);
        }
        sb.append("}\n");
    }
}
