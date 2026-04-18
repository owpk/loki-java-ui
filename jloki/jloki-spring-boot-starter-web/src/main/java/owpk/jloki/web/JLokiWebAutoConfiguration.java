package owpk.jloki.web;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import owpk.jloki.advisor.llm.AnalysisEventBus;
import owpk.jloki.advisor.llm.JLokiLlmAutoConfiguration;
import owpk.jloki.starter.JLokiAutoConfiguration;

@AutoConfiguration(after = {JLokiAutoConfiguration.class, JLokiLlmAutoConfiguration.class})
@Import(LokiController.class)
public class JLokiWebAutoConfiguration {

    @Bean
    @ConditionalOnBean(AnalysisEventBus.class)
    AnalysisController analysisController(AnalysisEventBus analysisEventBus) {
        return new AnalysisController(analysisEventBus);
    }
}
