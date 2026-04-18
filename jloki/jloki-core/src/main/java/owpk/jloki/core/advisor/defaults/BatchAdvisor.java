package owpk.jloki.core.advisor.defaults;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import owpk.jloki.core.advisor.LokiExecutionContext;
import owpk.jloki.core.advisor.LokiStreamAdvisor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public record BatchAdvisor(int batchSize, Consumer<List<?>> batchConsumer) implements LokiStreamAdvisor {
    private static final Logger log = LoggerFactory.getLogger(BatchAdvisor.class);

    @Override
    public <T> Flux<T> aroundFlux(URI uri, Flux<T> upstream, LokiExecutionContext ctx) {
        return Flux.defer(() -> {
            List<T> buffer = new ArrayList<>(batchSize);
            return upstream.doOnNext(item -> {
                buffer.add(item);
                if (buffer.size() >= batchSize) {
                    logBatch(buffer);
                    buffer.clear();
                }
            }).doOnComplete(() -> {
                if (!buffer.isEmpty()) {
                    logBatch(buffer);
                }
            });
        });
    }

    private <T> void logBatch(List<T> batch) {
        batchConsumer.accept(batch);
    }

    @Override
    public <T> Mono<T> aroundMono(URI uri, Mono<T> upstream, LokiExecutionContext ctx) {
        return upstream;
    }
}