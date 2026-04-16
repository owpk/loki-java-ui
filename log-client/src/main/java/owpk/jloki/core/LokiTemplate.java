package owpk.jloki.core;

import java.net.URI;
import java.util.function.Consumer;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import owpk.jloki.core.dsl.LokiQueryRangeRequest;
import owpk.jloki.core.dsl.LokiQueryRangeRequest.LokiQueryRangeRequestBuilder;
import owpk.jloki.core.dsl.LokiQueryRequest;
import owpk.jloki.core.dsl.LokiTailRequest;
import owpk.jloki.core.model.LokiTailResponse;
import owpk.jloki.core.model.PushLogRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
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
 * предназначен для частой выборочной чистки. Если вам нужно часто удалять логи,
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
@Component
@Slf4j
public class LokiTemplate {
    private final ObjectMapper objectMapper;
    private final WebClient webClient;
    private final LokiSettingsProvider settingsProvider;

    public LokiTemplate(ObjectMapper objectMapper, LokiSettingsProvider settingsProvider) {
        this.objectMapper = objectMapper;
        this.settingsProvider = settingsProvider;
        this.webClient = WebClient.builder().baseUrl(settingsProvider.provide().lokiBaseUrl()).build();
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
            var client = new ReactorNettyWebSocketClient();

            var uri = request.toURIFn().apply(st().lokiBaseUrl()); // сделай ws:// тут
            logUri(uri);

            return Flux.<LokiTailResponse<T>>create(sink -> {

                client.execute(uri, session -> session.receive()
                        .map(WebSocketMessage::getPayloadAsText)
                        .flatMap(payload -> {
                            try {
                                return Mono.just(objectMapper.readValue(payload, ref));
                            } catch (Exception e) {
                                log.error("Failed to parse message: {}", payload, e);
                                return Mono.empty();
                            }
                        })
                        .doOnNext(sink::next)
                        .doOnError(sink::error)
                        .doOnComplete(sink::complete)
                        .then()).subscribe();

            }, FluxSink.OverflowStrategy.BUFFER);
        });
    }

    private void logUri(URI uri) {
        log.info(uri.toString());
    }

    private LokiTemplateSettings st() {
        return settingsProvider.provide();
    }

}