package owpk.jloki.core.dsl.expresson;

public record FnExpr(String fn, Expression inner) implements Expression {

    public static String fn(String fnName, String body) {
        return "%s (%s)".formatted(fnName, body);
    }

    @Override
    public String eval() {
        return fn(fn, inner.eval());
    }

    @Override
    public String pretty(int indent) {
        String pad = "    ".repeat(indent);

        return pad + fn + "(\n"
                + inner.pretty(indent + 1)
                + "\n" + pad + ")";
    }
}
