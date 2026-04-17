package owpk.jloki.core.dsl.expresson;

public record PipeExpr(Expression inner) implements Expression {

    @Override
    public String eval() {
        return "| " + inner.eval();
    }

    @Override
    public String pretty(int indent) {
        return "    ".repeat(indent) + "| " + inner.eval();
    }
}
