package owpk.jloki.core.moddsl.expresson;

public record QueryExpr(String q) implements Expression {
    
    @Override
    public String render() {
        return q;
    }

    @Override
    public String pretty(int indent) {
        return "    ".repeat(indent) + q;
    }
}
