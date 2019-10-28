package ir3.ast;

import java.util.List;

public class CData3 implements Ir3Node {
    final Type3 cname;
    final List<VarDecl3> fields;

    public CData3(Type3 cname, List<VarDecl3> fields) {
        this.cname = cname;
        this.fields = fields;
    }
}