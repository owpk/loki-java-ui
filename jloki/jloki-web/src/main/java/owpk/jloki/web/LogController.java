package owpk.jloki.web;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import owpk.jloki.core.model.LogFilterStreamRequest;
import owpk.jloki.core.service.StreamingService;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@Slf4j
public class LogController {

    private final StreamingService<Object> logStreamingService;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Object> streamLogs(
            @RequestBody LogFilterStreamRequest filter,
            @RequestParam(required = false, defaultValue = "2") int delaySec) {
        return logStreamingService.stream(filter, delaySec);
    }
}
