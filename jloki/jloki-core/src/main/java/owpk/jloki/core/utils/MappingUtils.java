package owpk.jloki.core.utils;

import java.util.List;
import java.util.Objects;

import owpk.jloki.core.dsl.LokiQueryDSL.LabelMatcher;
import owpk.jloki.core.dsl.expresson.LabelSelector;
import owpk.jloki.core.dsl.utils.LabelEntry;
import owpk.jloki.core.model.LogQueryFilter;

public final class MappingUtils {
    private MappingUtils() {
    }

    public static LabelSelector toLabelQuery(List<LogQueryFilter> filter) {
        var labelQuery = new LabelSelector();
        if (filter != null) {
            filter.stream()
                    .filter(Objects::nonNull)
                    .filter(it -> it.value() != null)
                    .map(it -> new LabelEntry(it.field(), LabelMatcher.of(it.operator()), it.value()))
                    .forEach(labelQuery::add);
        }
        return labelQuery;
    }
}
