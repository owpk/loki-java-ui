package owpk.jloki.core.advisor;

import java.net.URI;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LokiStreamAdvisor {

    <T> Mono<T> aroundMono(
            URI uri,
            Mono<T> upstream,
            LokiExecutionContext ctx);

    <T> Flux<T> aroundFlux(
            URI uri,
            Flux<T> upstream,
            LokiExecutionContext ctx);
}
