package owpk.jloki.core.dsl.expresson;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record LokiQuery(
        LabelSelector selector,
        List<Expression> body) implements Expression {

    @Override
    public String eval() {
        var sb = new StringBuilder();
        var selectorRaw = selector.eval();
        if (selectorRaw != null && !selectorRaw.isBlank())
            sb.append(selector.eval()).append(" ");

        var rawBody = body.stream().filter(Objects::nonNull)
                .map(it -> it.eval())
                .collect(Collectors.joining(" "));

        sb.append(rawBody);
        return sb.toString();
    }

    @Override
    public String pretty(int indent) {
        var sb = new StringBuilder();
        sb.append(selector.pretty(indent));
        var raw = body.stream()
                .map(it -> it.pretty(indent + 1))
                .collect(Collectors.joining("\n"));
        return sb.append(raw).toString();
    }

    public String pretty() {
        return pretty(0);
    }
}
