package owpk.jloki.core;

import java.net.URI;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * --- Сортировка
 * Логи в Loki можно сортировать по временной метке, но не по содержимому
 * или другим полям. Loki был разработан так, чтобы записи внутри одного
 * лог-потока всегда хранились в порядке их поступления, что делает сортировку
 * по времени очень эффективной.
 * 
 * --- Удаление логов:
 * Важные соображения
 * Осторожность: Удаление логов в Loki необратимо. После отправки запроса и
 * истечения периода отмены (обычно 24 часа) восстановить данные будет
 * невозможно.
 * 
 * Производительность: Удаление логов — это ресурсоёмкая операция. Loki не
 * предназначен для частой выборочной чистки. Если нужно часто удалять логи,
 * возможно, стоит пересмотреть, какие данные вы вообще отправляете в Loki.
 * 
 * Индексы и хранилище: Удаление логов работает только с определенными типами
 * хранилищ индексов, такими как boltdb-shipper или tsdb.
 * 
 * Задержка: Удаление не происходит мгновенно. Логи могут быть видны в поиске
 * ещё некоторое время после отправки запроса.
 * 
 * Управление хранением: Для регулярной очистки старых данных гораздо
 * эффективнее использовать политики хранения (retention policies), которые
 * настраиваются в конфигурации Loki. Это автоматизирует удаление логов старше
 * определённого возраста.
 * ---
 * 
 * @author Vyacheslav Vorobev
 */
@Slf4j
public class LokiTemplate {
    private final ObjectMapper objectMapper;
    private final WebClient webClient;
    private final LokiSettingsProvider settingsProvider;
    private final ReactorNettyWebSocketClient webSocketClient;
    private final LokiStreamPipeline lokiStreamPipeline = new LokiStreamPipeline();

    public LokiTemplate(
            ReactorNettyWebSocketClient webSocketClient,
            WebClient webClient,
            ObjectMapper objectMapper,
            String lokiBaseUrl,
            List<LokiStreamAdvisor> advisors) {
        this(webSocketClient, webClient, objectMapper,
                new DefaultLokiSettingsProvider(new LokiTemplateSettings(lokiBaseUrl)),
                advisors);
    }

    public LokiTemplate(
            ReactorNettyWebSocketClient webSocketClient,
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
        return this.query(request, new ParameterizedTypeReference<Object>() {
        });
    }

    public <T> Mono<T> query(LokiQueryRequest request, ParameterizedTypeReference<T> ref) {
        var lokiBaseUrl = st().lokiBaseUrl();
        var queryPath = st().queryPath();
        var uri = request.toURIFn().apply(lokiBaseUrl + queryPath);
        logUri(uri);

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(ref)
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
        return this.queryRange(request, new ParameterizedTypeReference<Object>() {
        });
    }

    public Mono<Object> queryRange(LokiQueryRangeRequest request) {
        return this.queryRange(request, new ParameterizedTypeReference<Object>() {
        });
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

    public <T> Flux<LokiTailResponse<T>> tailLogsStream(
            LokiTailRequest request,
            TypeReference<LokiTailResponse<T>> ref) {
        return Flux.defer(() -> {

            var uri = request.toURIFn()
                    .apply(st().lokiBaseUrl() + st().tailPath());

            logUri(uri);

            var raw = parseStream(webSocketTextStream(uri), ref);
            return lokiStreamPipeline.executeFlux(uri, raw, new LokiExecutionContext());
        });
    }

    private <T> Flux<T> parseStream(Flux<WebSocketMessage> stream, TypeReference<T> ref) {
        return stream.flatMap(payload -> {
            try {
                var asString = payload.getPayloadAsText();
                var parsed = objectMapper.readValue(asString, ref);
                return Mono.just(parsed);
            } catch (Exception e) {
                log.error("Failed to parse: {}", payload, e);
                return Mono.empty();
            }
        });
    }

    private Flux<WebSocketMessage> webSocketTextStream(URI uri) {
        return Flux.<WebSocketMessage>defer(() -> {
            var client = webSocketClient;

            return Flux.create(sink -> {
                var raw = client.execute(uri, session -> session.receive()
                        .doOnNext(sink::next)
                        .doOnError(sink::error)
                        .doOnComplete(sink::complete)
                        .then());
                        
                var disposable = raw.subscribe();
                sink.onCancel(disposable::dispose);
                sink.onDispose(disposable::dispose);
            });
        });

    }

    private void logUri(URI uri) {
        log.info(uri.toString());
    }

    private LokiTemplateSettings st() {
        return settingsProvider.provide();
    }

}