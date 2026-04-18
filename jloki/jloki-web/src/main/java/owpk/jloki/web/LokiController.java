package owpk.jloki.web;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import owpk.jloki.core.dsl.LokiQueryRangeRequest;
import owpk.jloki.core.dsl.LokiQueryRequest;
import owpk.jloki.core.model.LogFilterStreamRequest;
import owpk.jloki.core.service.QueryService;
import owpk.jloki.core.service.StreamingService;
import owpk.jloki.starter.JLokiProperties;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("#{jLokiProperties.apiPath}")
@RequiredArgsConstructor
@Slf4j
public class LokiController {

    private final StreamingService<Object> logStreamingService;
    private final QueryService<Object> queryService;
    private final JLokiProperties jLokiProperties;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Object> streamLogs(
            @RequestBody LogFilterStreamRequest filter,
            @RequestParam(required = false, defaultValue = "2") int delaySec) {
        return logStreamingService.stream(filter, delaySec);
    }

    @PostMapping("/query")
    public Mono<Object> queryLogs(@RequestBody LokiQueryRequest request) {
        return queryService.query(request);
    }

    @PostMapping("/queryRange")
    public Mono<Object> queryRangeLogs(@RequestBody LokiQueryRangeRequest request) {
        return queryService.queryRange(request);
    }

}
