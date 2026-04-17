package owpk.jloki.core.moddsl.steps;

import java.util.Map;
import java.util.stream.Collectors;

import owpk.jloki.core.moddsl.LokiQueryDSL.LabelMatcher;

public record SimpleLabelSelector(Map<String, LabelEntry> labels) implements LabelSelector {

    public SimpleLabelSelector label(String key, String value) {
        return label(key, LabelMatcher.EQ, value);
    }

    public SimpleLabelSelector label(String key, LabelMatcher matcher, String value) {
        if (value != null)
            this.labels.put(key, new LabelEntry(key, matcher, value));
        return this;
    }

    @Override
    public String render() {
        return labels.entrySet().stream()
                .map(e -> {
                    LabelEntry entry = e.getValue();
                    return "%s%s\"%s\"".formatted(entry.key(), entry.matcher().getValue(), entry.value());
                })
                .collect(Collectors.joining(",", "{", "}"));
    }
}
