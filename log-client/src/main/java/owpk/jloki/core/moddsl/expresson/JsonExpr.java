package owpk.jloki.core.moddsl.expresson;

public record JsonExpr() implements Expression {

    @Override
    public String render() {
        return "| json";
    }

    @Override
    public String pretty(int indent) {
        return "    ".repeat(indent) + "| json";
    }
}
