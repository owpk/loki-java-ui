package owpk.jloki.starter;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

import owpk.jloki.core.LokiTemplate;
import owpk.jloki.core.WebSocketClient;
import owpk.jloki.core.advisor.LokiStreamAdvisor;
import owpk.jloki.core.service.DefaultLokiService;
import owpk.jloki.core.service.QueryService;
import owpk.jloki.core.service.StreamingService;
import owpk.jloki.core.settings.DefaultLokiSettingsProvider;
import owpk.jloki.core.settings.LokiSettingsProvider;
import owpk.jloki.core.settings.LokiTemplateSettings;
import tools.jackson.databind.ObjectMapper;

@AutoConfiguration
@EnableConfigurationProperties(JLokiProperties.class)
@ConditionalOnProperty(prefix = "loki", name = "base-url", matchIfMissing = true)
public class JLokiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    WebSocketClient webSocketClient() {
        return new WebSocketClientFactory().createReactorNettyClient();
    }

    @Bean
    @ConditionalOnMissingBean
    WebClient webClient() {
        return WebClient.builder().build();
    }

    @Bean
    @ConditionalOnMissingBean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @ConditionalOnMissingBean
    LokiSettingsProvider lokiSettingsProvider(JLokiProperties properties) {
        var settings = new LokiTemplateSettings(
                properties.getBaseUrl(),
                properties.getQueryRangePath(),
                properties.getQueryPath(),
                properties.getTailPath(),
                properties.getPushPath()
        );
        return new DefaultLokiSettingsProvider(settings);
    }

    @Bean
    @ConditionalOnMissingBean
    LokiTemplate lokiTemplate(
            WebSocketClient webSocketClient,
            WebClient webClient,
            ObjectMapper objectMapper,
            LokiSettingsProvider settingsProvider,
            ObjectProvider<LokiStreamAdvisor> advisors) {
        return new LokiTemplate(webSocketClient, webClient, objectMapper, settingsProvider,
                advisors.stream().collect(Collectors.toList()));
    }

    @Bean
    DefaultLokiService lokiService(LokiTemplate lokiTemplate) {
        return new DefaultLokiService(lokiTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    StreamingService<Object> streamingService(LokiTemplate lokiTemplate) {
        return lokiService(lokiTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    QueryService<Object> queryService(LokiTemplate lokiTemplate) {
        return lokiService(lokiTemplate);
    }
}
