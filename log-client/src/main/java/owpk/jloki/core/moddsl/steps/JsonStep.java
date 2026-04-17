package owpk.jloki.core.moddsl.steps;

public record JsonStep() implements PipelineStep {

    @Override
    public String render() {
        return "| json";
    }
}
