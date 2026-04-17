package owpk.jloki.core.moddsl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import owpk.jloki.core.moddsl.LokiQueryDSL.Filter;
import owpk.jloki.core.moddsl.LokiQueryDSL.Regex;
import owpk.jloki.core.moddsl.expresson.Expression;
import owpk.jloki.core.moddsl.expresson.FilterExpr;
import owpk.jloki.core.moddsl.expresson.FnExpr;
import owpk.jloki.core.moddsl.expresson.JsonExpr;
import owpk.jloki.core.moddsl.expresson.LabelSelector;
import owpk.jloki.core.moddsl.expresson.LokiQuery;
import owpk.jloki.core.moddsl.expresson.PipelineExpr;
import owpk.jloki.core.moddsl.expresson.QueryExpr;
import owpk.jloki.core.moddsl.steps.LabelSelector.LabelEntry;

public class LokiQueryBuilder {

    private final LabelSelector labels = new LabelSelector();
    private final List<Expression> pipeline = new ArrayList<>();
    private Expression root;

    public LokiQueryBuilder label(Consumer<LabelEntry.LabelEntryBuilder> factory) {
        var builder = LabelEntry.builder();
        factory.accept(builder);
        var labelEntry = builder.build();
        labels.add(labelEntry);
        return this;
    }

    public LokiQueryBuilder label(String key, String value) {
        labels.add(new LabelEntry(key, value));
        return this;
    }

    public LokiQueryBuilder query(Consumer<LokiQueryBuilder> factory) {
        var builder = new LokiQueryBuilder();
        factory.accept(builder);
        var query = builder.build();
        this.root = new QueryExpr(query.render());
        return this;
    }

    public LokiQueryBuilder query(LokiQuery q) {
        this.root = new QueryExpr(q.render());
        return this;
    }

    public LokiQueryBuilder query(String q) {
        this.root = new QueryExpr(q);
        return this;
    }

    public LokiQueryBuilder filter(Filter op, String value) {
        pipeline.add(new FilterExpr(op, value));
        return this;
    }

    public LokiQueryBuilder regex(Regex key, String pattern) {
        pipeline.add(new FilterExpr(key, pattern));
        return this;
    }

    public LokiQueryBuilder regex(String pattern) {
        pipeline.add(new FilterExpr(Regex.REG_CONTAINS, pattern));
        return this;
    }

    public LokiQueryBuilder json() {
        pipeline.add(new JsonExpr());
        return this;
    }

    public LokiQueryBuilder fn(String fn, Expression inner) {
        pipeline.add(new FnExpr(fn, inner));
        return this;
    }

    /**
     * @see LokiQueryDSL.Fn to generate function
     */
    public LokiQueryBuilder fn(String fn, String inner) {
        pipeline.add(new FnExpr(fn, new QueryExpr(inner)));
        return this;
    }

    public LokiQuery build() {

        Expression base = root;

        if (!labels.isEmpty()) {
            base = new QueryExpr(labels.render());
                           
        }

        if (!pipeline.isEmpty()) {
            base = new PipelineExpr(pipeline, base);
        }

        return new LokiQuery(base);
    }
}
