package owpk.jloki.core.advisor.defaults;

import java.net.URI;

import lombok.extern.slf4j.Slf4j;
import owpk.jloki.core.advisor.LokiExecutionContext;
import owpk.jloki.core.advisor.LokiStreamAdvisor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class LoggingAdvisor implements LokiStreamAdvisor {

    @Override
    public <T> Flux<T> aroundFlux(URI uri, Flux<T> upstream, LokiExecutionContext ctx) {
        return upstream
                .doOnSubscribe(s -> log.info("Start stream: {}", uri))
                .doOnNext(v -> log.debug("event: {}", v))
                .doOnError(e -> log.error("stream error: {}", uri, e))
                .doFinally(sig -> log.info("stream finished: {}", sig));
    }

    @Override
    public <T> Mono<T> aroundMono(URI uri, Mono<T> upstream, LokiExecutionContext ctx) {
        return upstream
                .doOnSubscribe(s -> log.info("Start stream: {}", uri))
                .doOnNext(v -> log.debug("event: {}", v))
                .doOnError(e -> log.error("stream error: {}", uri, e))
                .doFinally(sig -> log.info("stream finished: {}", sig));
    }
}
