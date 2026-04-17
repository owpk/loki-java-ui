package owpk.jloki.core.moddsl.steps;

import lombok.Builder;
import owpk.jloki.core.moddsl.LokiQueryDSL.LabelMatcher;

public interface LabelSelector {

    @Builder
    public record LabelEntry(String key, LabelMatcher matcher, String value) {
        public LabelEntry(String key, String value) {
            this(key, LabelMatcher.EQ, value);
        }
    }

    String render();
}
