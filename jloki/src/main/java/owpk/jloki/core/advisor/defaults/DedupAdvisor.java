package owpk.jloki.core.advisor.defaults;

import java.net.URI;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import owpk.jloki.core.advisor.LokiExecutionContext;
import owpk.jloki.core.advisor.LokiStreamAdvisor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DedupAdvisor implements LokiStreamAdvisor {

    private final Set<Object> seen = ConcurrentHashMap.newKeySet();

    @Override
    public <T> Flux<T> aroundFlux(URI uri, Flux<T> upstream, LokiExecutionContext ctx) {
        return upstream.filter(event -> {
            var key = event.hashCode();
            return seen.add(key);
        });
    }

    @Override
    public <T> Mono<T> aroundMono(URI uri, Mono<T> upstream, LokiExecutionContext ctx) {
        return upstream.filter(event -> {
            var key = event.hashCode();
            return seen.add(key);
        });
    }
}
