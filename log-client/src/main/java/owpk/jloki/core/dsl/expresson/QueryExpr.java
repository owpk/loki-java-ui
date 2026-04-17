package owpk.jloki.core.dsl.expresson;

public record QueryExpr(String q) implements Expression {
    
    @Override
    public String eval() {
        return q;
    }

    @Override
    public String pretty(int indent) {
        return "    ".repeat(indent) + q;
    }
}
