package owpk.jloki.core.advisor;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public record LokiStreamPipeline(List<LokiStreamAdvisor> advisors) {

    public LokiStreamPipeline() {
        this(new ArrayList<>());
    }

    public void registerAdvisor(LokiStreamAdvisor advisor) {
        advisors.add(advisor);
    }

    public <T> Mono<T> executeMono(URI uri, Mono<T> source, LokiExecutionContext ctx) {
        Mono<T> result = source;
        for (var advisor : advisors)
            result = advisor.aroundMono(uri, result, ctx);
        return result;
    }

    public <T> Flux<T> executeFlux(URI uri, Flux<T> source, LokiExecutionContext ctx) {
        Flux<T> result = source;
        for (var advisor : advisors)
            result = advisor.aroundFlux(uri, result, ctx);
        return result;
    }
}
