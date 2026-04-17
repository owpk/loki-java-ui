package owpk.jloki.core.moddsl.expresson;

import owpk.jloki.core.moddsl.LokiQueryDSL.Filter;
import owpk.jloki.core.moddsl.LokiQueryDSL.Regex;

public record FilterExpr(String op, String value) implements Expression {

    public FilterExpr(Filter filter, String value) {
        this(filter.getValue(), value);
    }

    public FilterExpr(Regex regex, String value) {
        this(regex.getValue(), value);
    }

    @Override
    public String render() {
        return op + " \"" + value + "\"";
    }

    @Override
    public String pretty(int indent) {
        return "    ".repeat(indent) + " " + op + " \"" + value + "\"";
    }
}
