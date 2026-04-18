package owpk.jloki.core.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import owpk.jloki.core.LokiTemplate;
import owpk.jloki.core.dsl.LokiQueryDSL;
import owpk.jloki.core.model.LogEvent;
import owpk.jloki.core.model.LogFilterStreamRequest;
import owpk.jloki.core.utils.MappingUtils;
import reactor.core.publisher.Flux;
import tools.jackson.core.type.TypeReference;

public class DefaultLokiService implements StreamingService<Object> {

    private final LokiTemplate lokiTemplate;

    public DefaultLokiService(LokiTemplate lokiTemplate) {
        this.lokiTemplate = lokiTemplate;
    }

    @Override
    public Flux<Object> stream(LogFilterStreamRequest filter, int delaySec) {
        if (Objects.isNull(filter)) {
            filter = LogFilterStreamRequest.builder().build();
        }

        var labelQuery = MappingUtils.toLabelQuery(filter.filters());

        var lokiTailRequest = LokiQueryDSL.tailRequest()
                .delayFor(delaySec)
                .limit(filter.limit())
                .queryExpression(expr -> expr
                        .label(labelQuery)
                        .json());

        if (filter.start() != null) {
            lokiTailRequest.start(Instant.ofEpochMilli(filter.start()));
        }

        return lokiTemplate.tailLogsStream(lokiTailRequest.build(), new TypeReference<>() {})
                .flatMapIterable(it -> it.streams())
                .map(it -> it.stream())
                .onBackpressureBuffer(10_000)
                .mergeWith(Flux.interval(Duration.ofSeconds(15))
                        .map(i -> new LogEvent("0", "heartbeat", Map.of())));
    }
}
