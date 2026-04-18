package owpk.jloki.advisor.llm;

import java.time.Instant;
import java.util.List;

public record LogAnalysisResult(
        String analysis,
        String model,
        List<String> analyzedLogs,
        Instant timestamp) {
}
