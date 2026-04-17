package owpk.jloki.core.dsl.expresson;

public record JsonExpr() implements Expression {

    @Override
    public String eval() {
        return "json";
    }

    @Override
    public String pretty(int indent) {
        return "    ".repeat(indent) + "json";
    }
}
