package owpk.jloki.core.moddsl.steps;

import owpk.jloki.core.moddsl.LokiQueryDSL.Regex;

public record RegexStep(String key, String pattern) implements PipelineStep {

    public RegexStep(String pattern) {
        this(Regex.REG_CONTAINS, pattern);
    }

    public RegexStep(Regex key, String pattern) {
        this(key.getValue(), pattern);
    }

    @Override
    public String render() {
        return "%s \"%s\"".formatted(key, pattern);
    }
}
