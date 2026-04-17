package owpk.jloki.core.dsl.expresson;

import owpk.jloki.core.dsl.LokiQueryDSL.Filter;
import owpk.jloki.core.dsl.LokiQueryDSL.Regex;

public record FilterExpr(String op, String value) implements Expression {

    public FilterExpr(Filter filter, String value) {
        this(filter.getValue(), value);
    }

    public FilterExpr(Regex regex, String value) {
        this(regex.getValue(), value);
    }

    @Override
    public String eval() {
        return "%s \"%s\"".formatted(op, value);
    }

    @Override
    public String pretty(int indent) {
        return "    ".repeat(indent) + eval();
    }
}
