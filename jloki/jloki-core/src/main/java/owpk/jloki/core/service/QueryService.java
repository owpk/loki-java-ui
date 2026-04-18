package owpk.jloki.core.service;

import org.jspecify.annotations.NonNull;

import owpk.jloki.core.dsl.LokiQueryRangeRequest;
import owpk.jloki.core.dsl.LokiQueryRequest;
import reactor.core.publisher.Mono;

public interface QueryService<T> {

    Mono<T> query(@NonNull LokiQueryRequest request);

    Mono<T> queryRange(@NonNull LokiQueryRangeRequest request);
    
}
