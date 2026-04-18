package owpk.jloki.core.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.jspecify.annotations.NonNull;

import owpk.jloki.core.LokiTemplate;
import owpk.jloki.core.dsl.LokiQueryDSL;
import owpk.jloki.core.dsl.LokiQueryRangeRequest;
import owpk.jloki.core.dsl.LokiQueryRequest;
import owpk.jloki.core.dsl.LokiTailRequest;
import owpk.jloki.core.model.LogEvent;
import owpk.jloki.core.model.LogFilterStreamRequest;
import owpk.jloki.core.model.LokiTailResponse.Stream;
import owpk.jloki.core.utils.MappingUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.core.type.TypeReference;

public class DefaultLokiService implements StreamingService<Object>, QueryService<Object> {

    private final LokiTemplate lokiTemplate;

    public DefaultLokiService(LokiTemplate lokiTemplate) {
        this.lokiTemplate = lokiTemplate;
    }

    @Override
    public Flux<Object> stream(@NonNull LokiTailRequest request) {
        return defaultLokiStream(request);
    }

    @Override
    public Flux<Object> stream(@NonNull final LogFilterStreamRequest filter, int delaySec) {
        var local = filter;

        var labelQuery = MappingUtils.toLabelQuery(local.filters());

        var lokiTailRequest = LokiQueryDSL.tailRequest()
                .delayFor(delaySec)
                .limit(local.limit())
                .queryExpression(expr -> expr
                        .label(labelQuery)
                        .json());

        if (local.start() != null)
            lokiTailRequest.start(Instant.ofEpochMilli(local.start()));

        return defaultLokiStream(lokiTailRequest.build());
    }

    public Flux<Object> defaultLokiStream(@NonNull final LokiTailRequest tailRequest) {
        return lokiTemplate.tailLogsStream(tailRequest, new TypeReference<>() {})
                .flatMapIterable(it -> it.streams())
                .map(Stream::stream)
                .onBackpressureBuffer(10_000)
                .mergeWith(Flux.interval(Duration.ofSeconds(15))
                        .map(i -> new LogEvent("0", "heartbeat", Map.of())));
    }

    @Override
    public Mono<Object> query(@NonNull final LokiQueryRequest request) {
        return lokiTemplate.query(request);
    }

    @Override
    public Mono<Object> queryRange(@NonNull final LokiQueryRangeRequest request) {
        return lokiTemplate.queryRange(request);
    }

}
