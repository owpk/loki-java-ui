package owpk.jloki.core.moddsl.expresson;

import java.util.List;

public record PipelineExpr(List<Expression> steps, Expression inner) implements Expression {

    @Override
    public String render() {
        var sb = new StringBuilder(inner.render());

        for (var step : steps)
            sb.append(" ").append(step.render());

        return sb.toString();
    }

    @Override
    public String pretty(int indent) {
        String pad = "    ".repeat(indent);

        StringBuilder sb = new StringBuilder();
        sb.append(inner.pretty(indent + 1)).append("\n");

        for (var step : steps) {
            sb.append(pad).append("    | ").append(step.pretty(indent)).append("\n");
        }

        return sb.toString();
    }
}
