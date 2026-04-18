package owpk.jloki.advisor.llm;

import java.time.Duration;

public record LlmAdvisorConfig(
        String baseUrl,
        String model,
        int batchSize,
        String systemPrompt,
        Duration timeout) {

    private static final String DEFAULT_PROMPT =
            "You are a log analyzer. Analyze the following log entries and provide a concise summary " +
            "of any errors, warnings, anomalies or patterns. Focus on actionable insights.";

    public static LlmAdvisorConfig of(String baseUrl, String model) {
        return new LlmAdvisorConfig(baseUrl, model, 20, DEFAULT_PROMPT, Duration.ofSeconds(60));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String baseUrl = "http://localhost:11434";
        private String model = "llama3";
        private int batchSize = 20;
        private String systemPrompt = DEFAULT_PROMPT;
        private Duration timeout = Duration.ofSeconds(60);

        public Builder baseUrl(String baseUrl) { this.baseUrl = baseUrl; return this; }
        public Builder model(String model) { this.model = model; return this; }
        public Builder batchSize(int batchSize) { this.batchSize = batchSize; return this; }
        public Builder systemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; return this; }
        public Builder timeout(Duration timeout) { this.timeout = timeout; return this; }

        public LlmAdvisorConfig build() {
            return new LlmAdvisorConfig(baseUrl, model, batchSize, systemPrompt, timeout);
        }
    }
}
