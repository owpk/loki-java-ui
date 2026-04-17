package owpk.jloki.core.advisor.defaults;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import lombok.extern.slf4j.Slf4j;
import owpk.jloki.core.advisor.LokiExecutionContext;
import owpk.jloki.core.advisor.LokiStreamAdvisor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public record BatchAdvisor(int batchSize, Consumer<List<?>> batchConsumer) implements LokiStreamAdvisor {

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
