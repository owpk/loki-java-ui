package owpk.jloki.core.service;

import owpk.jloki.core.dsl.LokiQueryRequest;
import reactor.core.publisher.Mono;

public interface QueryService<T> {

    public Mono<T> query(LokiQueryRequest request);
    
}
