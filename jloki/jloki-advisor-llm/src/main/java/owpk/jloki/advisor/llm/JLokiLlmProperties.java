package owpk.jloki.advisor.llm;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "loki.llm")
public class JLokiLlmProperties {

    private boolean enabled = true;
    private String baseUrl = "http://localhost:11434";
    private String model = "llama3";
    private int batchSize = 20;
    private int timeoutSec = 60;
    private String systemPrompt =
            "You are a log analyzer. Analyze the following log entries and provide a concise summary " +
            "of any errors, warnings, anomalies or patterns. Focus on actionable insights.";

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public int getBatchSize() { return batchSize; }
    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }

    public int getTimeoutSec() { return timeoutSec; }
    public void setTimeoutSec(int timeoutSec) { this.timeoutSec = timeoutSec; }

    public String getSystemPrompt() { return systemPrompt; }
    public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }
}
