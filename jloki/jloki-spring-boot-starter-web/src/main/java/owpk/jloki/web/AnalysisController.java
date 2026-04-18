package owpk.jloki.web;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import owpk.jloki.advisor.llm.AnalysisEventBus;
import owpk.jloki.advisor.llm.LogAnalysisResult;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("${loki.api-path:/api/loki}")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisEventBus analysisEventBus;

    @GetMapping(value = "/analysis/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<LogAnalysisResult> streamAnalysis() {
        return analysisEventBus.results();
    }
}
