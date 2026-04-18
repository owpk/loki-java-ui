package owpk.jloki.starter;

import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

import owpk.jloki.core.LokiTemplate;
import owpk.jloki.core.WebSocketClient;
import owpk.jloki.core.service.DefaultLokiService;
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
            LokiSettingsProvider settingsProvider) {
        return new LokiTemplate(webSocketClient, webClient, objectMapper, settingsProvider, List.of());
    }

    @Bean
    @ConditionalOnMissingBean
    StreamingService<Object> streamingService(LokiTemplate lokiTemplate) {
        return new DefaultLokiService(lokiTemplate);
    }
}
