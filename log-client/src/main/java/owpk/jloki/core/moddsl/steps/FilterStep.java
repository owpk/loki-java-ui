package owpk.jloki.core.moddsl.steps;

import owpk.jloki.core.moddsl.LokiQueryDSL.Filter;

public record FilterStep(String op, String value) implements PipelineStep {

    public FilterStep(Filter op, String val) {
        this(op.getValue(), val);
    }

    @Override
    public String render() {
        return op + " \"" + value + "\"";
    }
}
