package com.example.logclient;

import java.net.URI;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;

import com.example.logclient.dsl.LokiQueryRangeRequest;
import com.example.logclient.dsl.LokiQueryRangeRequest.LokiQueryRangeRequestBuilder;
import com.example.logclient.dsl.LokiQueryRequest;
import com.example.logclient.dsl.LokiTailRequest;
import com.example.logclient.model.LokiTailResponse;
import com.example.logclient.model.PushLogRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
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
public class LokiRestClient {
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ReactorNettyWebSocketClient webSocketClient;
    private final WebClient webClient;

    private final String lokiBaseUrl;
    private final String queryRangePath;
    private final String queryPath;
    private final String tailPath;
    private final String pushPath;

    @PostConstruct
    public void init() {
        objectMapper.registerModule(new JavaTimeModule());            
    }

    public LokiRestClient(
            ReactorNettyWebSocketClient webSocketClient,
            @Value("${loki.base-url}") String lokiBaseUrl,
            @Value("${loki.query-path:/loki/api/v1/query_range}") String queryRangePath,
            @Value("${loki.query-path:/loki/api/v1/query}") String queryPath,
            @Value("${loki.tail-path:/loki/api/v1/tail}") String tailPath,
            @Value("${loki.tail-path:/loki/api/v1/push}") String pushPath) {
        this.lokiBaseUrl = lokiBaseUrl;
        this.queryRangePath = queryRangePath;
        this.queryPath = queryPath;
        this.tailPath = tailPath;
        this.pushPath = pushPath;

        this.webClient = WebClient.builder().baseUrl(lokiBaseUrl).build();
        this.webSocketClient = webSocketClient;
    }

    public Mono<Void> push(PushLogRequest log) {
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

    public Mono<Void> tailLogsStream(LokiTailRequest streamRequest, WebSocketHandler handler) {
        var uri = streamRequest.toURIFn().apply(lokiBaseUrl + tailPath);
        logUri(uri);
        return webSocketClient.execute(uri, handler);
    }

    public <T> Flux<LokiTailResponse<T>> tailLogsStream(LokiTailRequest request, TypeReference<LokiTailResponse<T>> ref) {
        return Flux.create(sink -> {
            WebSocketHandler handler = session -> session.receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .flatMap(it -> {
                        try {
                            var mapped = objectMapper.readValue(it, ref);
                            return Mono.just(mapped);
                        } catch (Exception e) {
                            log.error("Failed to parse message: {}", it, e);
                            return Mono.empty();
                        }
                    })
                    .doOnNext(sink::next)
                    .doOnError(sink::error)
                    .doFinally(signal -> sink.complete())
                    .then();

            this.tailLogsStream(request, handler).subscribe(
                    null,
                    sink::error,
                    sink::complete);
        });
    }

    private void logUri(URI uri) {
        log.info(uri.toString());
    }

}