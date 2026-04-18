package owpk.jloki.advisor.llm;

import java.time.Duration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(JLokiLlmProperties.class)
@ConditionalOnProperty(prefix = "loki.llm", name = "enabled", matchIfMissing = true)
public class JLokiLlmAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AnalysisEventBus analysisEventBus() {
        return new AnalysisEventBus();
    }

    @Bean
    @ConditionalOnMissingBean
    public LlmStreamAdvisor llmStreamAdvisor(JLokiLlmProperties props, AnalysisEventBus bus) {
        var config = LlmAdvisorConfig.builder()
                .baseUrl(props.getBaseUrl())
                .model(props.getModel())
                .batchSize(props.getBatchSize())
                .systemPrompt(props.getSystemPrompt())
                .timeout(Duration.ofSeconds(props.getTimeoutSec()))
                .build();
        return new LlmStreamAdvisor(config, bus::publish);
    }
}
