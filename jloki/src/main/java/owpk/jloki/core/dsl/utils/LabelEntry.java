package owpk.jloki.core.dsl.utils;

import lombok.Builder;
import owpk.jloki.core.dsl.LokiQueryDSL.LabelMatcher;

@Builder
public record LabelEntry(String key, LabelMatcher matcher, String value) {
    public LabelEntry(String key, String value) {
        this(key, LabelMatcher.EQ, value);
    }
}
