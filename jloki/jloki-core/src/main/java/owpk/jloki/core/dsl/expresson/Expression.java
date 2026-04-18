package owpk.jloki.core.dsl.expresson;

public sealed interface Expression permits
        FilterExpr, FnExpr, JsonExpr, LabelSelector, LokiQuery, PipeExpr, QueryExpr {

    String eval();

    String pretty(int indent);
}
