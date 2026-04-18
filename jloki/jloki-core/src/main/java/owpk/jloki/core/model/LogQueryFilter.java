package owpk.jloki.core.model;

import lombok.Builder;
import owpk.jloki.core.dsl.LokiQueryDSL.LabelMatcher;

@Builder
public record LogQueryFilter(
        String field, // например "message"
        String operator, // =, !=, =~, !~
        String value // regex или строка
) {

    public LogQueryFilter {
        if (field != null && value != null && operator == null) {
            operator = LabelMatcher.EQ.getValue();
        }
    }
}