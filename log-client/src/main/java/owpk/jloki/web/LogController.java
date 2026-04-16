package owpk.jloki.web;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import owpk.jloki.core.LokiTemplate;
import owpk.jloki.core.api.LokiQuerySupported;
import owpk.jloki.core.dsl.LokiQueryRangeRequest;
import owpk.jloki.core.model.LogFilterStreamRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@Slf4j
public class LogController {

    private final LokiTemplate lokiTemplate;

    @PostMapping("/loki/range_request")
    public Flux<Object> lokiRangeRequest(@RequestBody String entity) {
        var req = LokiQueryRangeRequest.builder().build();
        return lokiTemplate.rangeRequest(req);
    }
    
    @GetMapping(value = "/{app}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Object> streamLogs(
            @PathVariable String app,
            @RequestParam(required = false) LogFilterStreamRequest filter,
            @RequestParam(required = false, defaultValue = "2") int delaySec) {
        
        if (filter == null)
            filter = new LogFilterStreamRequest();
        return lokiTemplate.tailLogsStream(null, null).map(it -> it.);
    }
}
