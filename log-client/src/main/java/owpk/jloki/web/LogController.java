package owpk.jloki.web;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import owpk.jloki.core.LokiTemplate;
import owpk.jloki.core.dsl.LokiTailRequest;
import owpk.jloki.core.model.LogFilterStreamRequest;
import owpk.jloki.web.dto.LogEvent;
import owpk.jloki.web.utils.MappingUtils;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@Slf4j
public class LogController {

    private final LokiTemplate lokiTemplate;

    // @PostMapping("/loki/range_request")
    // public Flux<Object> lokiRangeRequest(@RequestBody String entity) {
    // var req = LokiQueryRangeRequest.builder().build();
    // return lokiTemplate.rangeRequest(req);
    // }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Object> streamLogs(
            @RequestBody LogFilterStreamRequest filter,
            @RequestParam(required = false, defaultValue = "2") int delaySec) {

        if (Objects.isNull(filter))
            filter = LogFilterStreamRequest.builder().build();

        var labelQuery = MappingUtils.toLabelQuery(filter.filters());

        var lokiTailRequest = LokiTailRequest.builder()
                .delayFor(delaySec)
                .limit(filter.limit())
                .queryExpression(expr -> expr
                        .labelQuery(labelQuery)
                        .pipe().query("json")
                        .log());

        if (filter.start() != null)
            lokiTailRequest.start(Instant.ofEpochMilli(filter.start()));

        return lokiTemplate.tailLogsStream(lokiTailRequest.build(), new TypeReference<>() {
        })
                .flatMapIterable(it -> it.streams())
                .map(it -> it.stream())
                .onBackpressureBuffer(10_000)
                .mergeWith(Flux.interval(Duration.ofSeconds(15))
                        .map(i -> new LogEvent("0", "heartbeat", Map.of())));
    }

}
