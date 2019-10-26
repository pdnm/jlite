package ast;

import java.util.List;

public class Program implements AstNode {
    public final List<ClassDecl> classDecls;

    public Program(List<ClassDecl> classDecls) {
        this.classDecls = classDecls;
    }
}
