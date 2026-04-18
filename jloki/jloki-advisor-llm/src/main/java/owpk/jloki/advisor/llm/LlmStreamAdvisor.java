package owpk.jloki.advisor.llm;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import owpk.jloki.core.advisor.LokiExecutionContext;
import owpk.jloki.core.advisor.LokiStreamAdvisor;
import owpk.jloki.advisor.llm.client.OpenAiClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class LlmStreamAdvisor implements LokiStreamAdvisor {

    private static final Logger log = LoggerFactory.getLogger(LlmStreamAdvisor.class);

    private final OpenAiClient client;
    private final LlmAdvisorConfig config;
    private final List<AnalysisResultHandler> handlers;

    public LlmStreamAdvisor(LlmAdvisorConfig config, List<AnalysisResultHandler> handlers) {
        this.config = config;
        this.client = new OpenAiClient(config);
        this.handlers = handlers;
    }

    public LlmStreamAdvisor(LlmAdvisorConfig config, AnalysisResultHandler handler) {
        this(config, List.of(handler));
    }

    /**
     * Основной поток логов проходит насквозь без изменений.
     * Параллельно батчами отправляется в LLM — результат передаётся в handlers.
     */
    @Override
    public <T> Flux<T> aroundFlux(URI uri, Flux<T> upstream, LokiExecutionContext ctx) {
        return upstream.publish(shared -> {
            Flux<T> passThrough = shared;

            Flux<T> sideAnalysis = shared
                    .map(Object::toString)
                    .buffer(config.batchSize())
                    .flatMap(batch -> analyze(batch)
                            .doOnNext(result -> handlers.forEach(h -> h.handle(result)))
                            .onErrorResume(e -> {
                                log.warn("LLM analysis failed for batch of {} logs: {}", batch.size(), e.getMessage());
                                return Mono.empty();
                            }))
                    .thenMany(Flux.empty());

            return Flux.merge(passThrough, sideAnalysis);
        });
    }

    /**
     * Для одиночных запросов (query/queryRange) анализ не применяется —
     * ответ не содержит отдельных лог-строк в исходном виде.
     */
    @Override
    public <T> Mono<T> aroundMono(URI uri, Mono<T> upstream, LokiExecutionContext ctx) {
        return upstream;
    }

    private Mono<LogAnalysisResult> analyze(List<String> batch) {
        var userContent = String.join("\n", batch);
        return client.complete(userContent)
                .map(analysis -> new LogAnalysisResult(analysis, config.model(), batch, Instant.now()));
    }
}
