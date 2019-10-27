package ast;

public enum Operator {
    ADD("+"), SUB("-"), MUL("*"), DIV("/"),
    AND("&&"), OR("||"), GT(">"), LT("<"), GE(">="), LE("<="),
    EQ("=="), NEQ("!="), NEG("!"), UMINUS("-"), ASSIGN("=");

    public final String symbol;
    Operator(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return symbol;
    }
}
