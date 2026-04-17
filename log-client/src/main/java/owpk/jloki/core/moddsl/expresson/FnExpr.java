package owpk.jloki.core.moddsl.expresson;

public record FnExpr(String fn, Expression inner) implements Expression {

    @Override
    public String render() {
        return fn + "(" + inner.render() + ")";
    }

    @Override
    public String pretty(int indent) {
        String pad = "    ".repeat(indent);

        return pad + fn + "(\n"
                + inner.pretty(indent + 1)
                + "\n" + pad + ")";
    }
}
