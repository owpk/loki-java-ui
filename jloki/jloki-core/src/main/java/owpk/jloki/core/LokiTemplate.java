package owpk.jloki.core;

import java.net.URI;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import owpk.jloki.core.advisor.LokiExecutionContext;
import owpk.jloki.core.advisor.LokiStreamAdvisor;
import owpk.jloki.core.advisor.LokiStreamPipeline;
import owpk.jloki.core.dsl.LokiQueryRangeRequest;
import owpk.jloki.core.dsl.LokiQueryRangeRequest.LokiQueryRangeRequestBuilder;
import owpk.jloki.core.dsl.LokiQueryRequest;
import owpk.jloki.core.dsl.LokiTailRequest;
import owpk.jloki.core.model.LokiTailResponse;
import owpk.jloki.core.model.PushLogRequest;
import owpk.jloki.core.settings.DefaultLokiSettingsProvider;
import owpk.jloki.core.settings.LokiSettingsProvider;
import owpk.jloki.core.settings.LokiTemplateSettings;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Slf4j
public class LokiTemplate {
    private final ObjectMapper objectMapper;
    private final WebClient webClient;
    private final LokiSettingsProvider settingsProvider;
    private final WebSocketClient webSocketClient;
    private final LokiStreamPipeline lokiStreamPipeline = new LokiStreamPipeline();

    public LokiTemplate(
            WebSocketClient webSocketClient,
            WebClient webClient,
            ObjectMapper objectMapper,
            String lokiBaseUrl,
            List<LokiStreamAdvisor> advisors) {
        this(webSocketClient, webClient, objectMapper,
                new DefaultLokiSettingsProvider(new LokiTemplateSettings(lokiBaseUrl)),
                advisors);
    }

    public LokiTemplate(
            WebSocketClient webSocketClient,
            WebClient webClient,
            ObjectMapper objectMapper,
            LokiSettingsProvider settingsProvider,
            List<LokiStreamAdvisor> advisors) {
        this.webSocketClient = webSocketClient;
        this.objectMapper = objectMapper;
        this.settingsProvider = settingsProvider;
        this.webClient = webClient;
        advisors.forEach(lokiStreamPipeline::registerAdvisor);
    }

    public Mono<Void> push(PushLogRequest log) {
        var lokiBaseUrl = st().lokiBaseUrl();
        var pushPath = st().pushPath();
        return webClient.post()
                .uri(lokiBaseUrl, builder -> builder.path(pushPath).build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(log)
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Mono<Object> query(LokiQueryRequest request) {
        return this.query(request, Object.class);
    }

    public <T> Mono<T> query(LokiQueryRequest request, Class<T> clazz) {
        var lokiBaseUrl = st().lokiBaseUrl();
        var queryPath = st().queryPath();
        var uri = request.toURIFn().apply(lokiBaseUrl + queryPath);
        logUri(uri);

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(clazz)
                .onErrorResume(e -> {
                    log.error("{}", e.getMessage(), e);
                    return Mono.empty();
                });
    }

    public <T> Mono<T> queryRange(Consumer<LokiQueryRangeRequestBuilder> request, ParameterizedTypeReference<T> ref) {
        var builder = LokiQueryRangeRequest.builder();
        request.accept(builder);
        return this.queryRange(builder.build(), ref);
    }

    public Mono<Object> queryRange(Consumer<LokiQueryRangeRequestBuilder> request) {
        return this.queryRange(request, new ParameterizedTypeReference<>() {});
    }

    public Mono<Object> queryRange(LokiQueryRangeRequest request) {
        return this.queryRange(request, new ParameterizedTypeReference<>() {});
    }

    public <T> Mono<T> queryRange(LokiQueryRangeRequest request, ParameterizedTypeReference<T> ref) {
        var lokiBaseUrl = st().lokiBaseUrl();
        var queryRangePath = st().queryRangePath();
        var uri = request.toURIFn().apply(lokiBaseUrl + queryRangePath);
        logUri(uri);

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(ref)
                .onErrorResume(e -> {
                    log.error("{}", e.getMessage(), e);
                    return Mono.error(() -> new RuntimeException(e));
                });
    }

    /**
     * @see owpk.jloki.core.dsl.LokiQueryDSL to build loki tail request
     * Example: tailLogSteam(LokiQueryDsl.tailRequest()...)
     * 
     * @param <T> - loki object
     * @param request - loki tail request
     * @param ref - mapper
     * @return - stream of loki entites
     */
    public <T> Flux<LokiTailResponse<T>> tailLogsStream(
            LokiTailRequest request,
            TypeReference<LokiTailResponse<T>> ref) {
        return Flux.defer(() -> {
            var uri = request.toURIFn()
                    .apply(st().lokiBaseUrl() + st().tailPath());

            logUri(uri);

            var raw = parseStream(webSocketClient.connectAndReceive(uri), ref);
            return lokiStreamPipeline.executeFlux(uri, raw, new LokiExecutionContext());
        });
    }

    private <T> Flux<T> parseStream(Flux<String> stream, TypeReference<T> ref) {
        return stream.flatMap(payload -> {
            try {
                var parsed = objectMapper.readValue(payload, ref);
                return Mono.just(parsed);
            } catch (Exception e) {
                log.error("Failed to parse: {}", payload, e);
                return Mono.empty();
            }
        });
    }

    private void logUri(URI uri) {
        log.info(uri.toString());
    }

    private LokiTemplateSettings st() {
        return settingsProvider.provide();
    }

}