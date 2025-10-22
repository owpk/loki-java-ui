package com.example.web;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.logclient.dsl.LokiQueryRangeRequest;
import com.example.model.BackendLog;
import com.example.model.LogFilterRequest;
import com.example.model.LogFilterStreamRequest;
import com.example.service.LogBackendService;
import com.example.service.LokiQuerySupported;
import com.example.web.dto.LogListResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@Slf4j
public class LogController {

    private final LogBackendService logBackendService;
    private final LokiQuerySupported<Object> lokiQuerySupportedService;

    @PostMapping("/loki/range_request")
    public Flux<Object> lokiRangeRequest(@RequestBody String entity) {
        var req = LokiQueryRangeRequest.builder().build();
        return lokiQuerySupportedService.rangeRequest(req);
    }
    

    @PostMapping("/{app}")
    public Mono<LogListResponse> getLogs(
            @PathVariable String app,
            @RequestBody LogFilterRequest filter) {
        
        log.info("{}", filter);
        return logBackendService.getLogs(app, filter)
                .collectList()
                .map(items -> LogListResponse.builder()
                        .items(items)
                        .total(items.size())
                        .build());
    }

    @GetMapping(value = "/{app}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BackendLog> streamLogs(
            @PathVariable String app,
            @RequestParam(required = false) LogFilterStreamRequest filter,
            @RequestParam(required = false, defaultValue = "2") int delaySec) {
        
        if (filter == null)
            filter = new LogFilterStreamRequest();
        return logBackendService.streamLogs(app, delaySec, filter);
    }
}
