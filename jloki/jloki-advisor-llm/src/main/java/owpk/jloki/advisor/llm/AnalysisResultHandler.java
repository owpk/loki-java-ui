package owpk.jloki.advisor.llm;

@FunctionalInterface
public interface AnalysisResultHandler {
    void handle(LogAnalysisResult result);
}
