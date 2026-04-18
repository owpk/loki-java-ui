package owpk.jloki.advisor.llm;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

public class AnalysisEventBus {

    private final Sinks.Many<LogAnalysisResult> sink = Sinks.many()
            .multicast()
            .onBackpressureBuffer(256, false);

    public void publish(LogAnalysisResult result) {
        sink.tryEmitNext(result);
    }

    public Flux<LogAnalysisResult> results() {
        return sink.asFlux();
    }
}
