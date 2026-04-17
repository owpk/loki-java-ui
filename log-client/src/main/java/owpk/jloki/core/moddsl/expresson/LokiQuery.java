package owpk.jloki.core.moddsl.expresson;

public record LokiQuery(Expression expr) implements Expression {

    public String render() {
        return expr.render();
    }

    public String pretty(int ident) {
        return expr.pretty(ident);
    }

    public String pretty() {
        return pretty(0);
    }
}
