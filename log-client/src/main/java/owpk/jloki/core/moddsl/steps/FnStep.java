package owpk.jloki.core.moddsl.steps;

public record FnStep(String fn) implements PipelineStep {

    public FnStep(String fnName, String inner) {
        this(fn(fnName, inner));
    }

    public static String fn(String fnName, String query) {
        return fnName + "(" + query + ")";
    }

    @Override
    public String render() {
        return fn;
    }
}
