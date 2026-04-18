package owpk.jloki.core.dsl.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.slf4j.Logger;

import lombok.extern.slf4j.Slf4j;
import owpk.jloki.core.dsl.LokiQueryDSL.Filter;
import owpk.jloki.core.dsl.LokiQueryDSL.Regex;
import owpk.jloki.core.dsl.expresson.Expression;
import owpk.jloki.core.dsl.expresson.FilterExpr;
import owpk.jloki.core.dsl.expresson.FnExpr;
import owpk.jloki.core.dsl.expresson.JsonExpr;
import owpk.jloki.core.dsl.expresson.LabelSelector;
import owpk.jloki.core.dsl.expresson.LokiQuery;
import owpk.jloki.core.dsl.expresson.PipeExpr;
import owpk.jloki.core.dsl.expresson.QueryExpr;

@Slf4j
public record LokiQueryBuilder(
        LabelSelector labels,
        List<Expression> body) {
    
    public LokiQueryBuilder() {
        this(new LabelSelector(), new ArrayList<>());
    }

    public LokiQueryBuilder label(LabelSelector selector) {
        selector.labels().forEach(labels::add);
        return this;
    }

    public LokiQueryBuilder label(String key, String value) {
        labels.add(new LabelEntry(key, value));
        return this;
    }

    public LokiQueryBuilder label(LabelEntry entry) {
        labels.add(entry);
        return this;
    }

    public LokiQueryBuilder query(Consumer<LokiQueryBuilder> factory) {
        var builder = new LokiQueryBuilder();
        factory.accept(builder);
        var query = builder.build();
        body.add(new QueryExpr(query.eval()));
        return this;
    }

    public LokiQueryBuilder query(LokiQuery q) {
        body.add(new QueryExpr(q.eval()));
        return this;
    }

    public LokiQueryBuilder query(String q) {
        body.add(new QueryExpr(q));
        return this;
    }

    public LokiQueryBuilder filter(Filter op, String value) {
        body.add(new FilterExpr(op, value));
        return this;
    }

    public LokiQueryBuilder filter(Regex key, String pattern) {
        body.add(new FilterExpr(key, pattern));
        return this;
    }

    public LokiQueryBuilder filter(String pattern) {
        body.add(new FilterExpr(Regex.REG_CONTAINS, pattern));
        return this;
    }

    public LokiQueryBuilder pipe(Expression expr) {
        body.add(new PipeExpr(expr));
        return this;
    }

    public LokiQueryBuilder json() {
        body.add(new PipeExpr(new JsonExpr()));
        return this;
    }

    public LokiQueryBuilder fn(String fn, Expression inner) {
        body.add(new FnExpr(fn, inner));
        return this;
    }

    public LokiQueryBuilder fn(String fn, String inner) {
        body.add(new FnExpr(fn, new QueryExpr(inner)));
        return this;
    }

    public LokiQueryBuilder log(BiConsumer<String, Logger> logger) {
        var query = new LokiQuery(labels, body);
        logger.accept(query.pretty(), log);
        return this;
    }

    public LokiQuery build() {
        return new LokiQuery(labels, body);
    }
}