package owpk.jloki.web;

import java.time.Instant;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import owpk.jloki.core.LokiTemplate;
import owpk.jloki.core.dsl.LokiTailRequest;
import owpk.jloki.core.model.LogFilterStreamRequest;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@Slf4j
public class LogController {

    private final LokiTemplate lokiTemplate;

    // @PostMapping("/loki/range_request")
    // public Flux<Object> lokiRangeRequest(@RequestBody String entity) {
    //     var req = LokiQueryRangeRequest.builder().build();
    //     return lokiTemplate.rangeRequest(req);
    // }
    
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Object> streamLogs(
            @RequestParam(required = false) LogFilterStreamRequest filter,
            @RequestParam(required = false, defaultValue = "2") int delaySec) {
        
        if (filter == null)
            filter = new LogFilterStreamRequest();

                var labelQuery = toLabelQuery(filter.getFilters());

        var lokiTailRequest = LokiTailRequest.builder()
                .delayFor(delaySec)
                .limit(filter.getLimit())
                .queryExpression(expr -> expr.labelQuery(labelQuery.label("app", app))
                        .pipe().query("json")
                        .log());

        if (filter.getStart() != null)
            lokiTailRequest.start(Instant.ofEpochMilli(filter.getStart()));

        return lokiTemplate.tailLogsStream(null, null)
            .flatMapIterable(it -> it.streams())
            .map(it -> it.stream());
    }
}
