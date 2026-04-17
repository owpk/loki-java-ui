package owpk.jloki.web.utils;

import java.util.List;
import java.util.Objects;

import owpk.jloki.core.dsl.LokiQueryExpression.LabelMatcher;
import owpk.jloki.core.dsl.LokiQueryExpression.LabelQueryBuilder;
import owpk.jloki.core.model.LogQueryFilter;

public final class MappingUtils {
    private MappingUtils() {}

    public static LabelQueryBuilder toLabelQuery(List<LogQueryFilter> filter) {
        var labelQuery = new LabelQueryBuilder();
        if (filter != null)
            filter.stream()
                    .filter(Objects::nonNull)
                    .filter(it -> it.value() != null)
                    .forEach(f -> labelQuery.label(f.field(), LabelMatcher.of(f.operator()), f.value()));
        return labelQuery;
    }
}
