package owpk.jloki;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;

import owpk.jloki.core.LokiTemplate;
import owpk.jloki.core.dsl.LokiQueryExpression.LabelMatcher;
import owpk.jloki.core.dsl.LokiQueryRangeRequest;
import owpk.jloki.core.model.LokiRawQueryResponse;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.WebsocketClientSpec;

@Configuration
public class BeanConfig {

    // @Bean
    ApplicationRunner expr(LokiTemplate lokiTemplate) {
        return args -> {
            var rangeRequest = LokiQueryRangeRequest.builder()
                    .queryExpression(lokiExpression -> lokiExpression
                            .labelQuery(labelQuery -> labelQuery
                                    .label("job", "mock-service")
                                    .label("message", LabelMatcher.REG_EQ, ".*timeout.*"))
                            .pipe().query("json")
                            .log())
                    .limit(50)
                    // .end(Instant.ofEpochMilli(1760726370553986880L))
                    .build();

            lokiTemplate.queryRange(rangeRequest, 
                new ParameterizedTypeReference<LokiRawQueryResponse<Object>>() {})
                    .map(it -> it.getData())
                    .flatMapIterable(it -> it.getResult())
                    .doOnNext(it -> {
                        System.out.println(it.getStream());
                        System.out.println(":::");
                        it.getValues().stream()
                                .flatMap(val -> val.stream())
                                .forEach(val -> System.out.println(val));
                        System.out.println("--------");
                    })
                    .subscribe();
        };
    }

    @Bean
    ReactorNettyWebSocketClient reactorNettyWebSocketClient() {
        var spec = WebsocketClientSpec.builder().maxFramePayloadLength(10048576);
        var httpClient = HttpClient.create();
        return new ReactorNettyWebSocketClient(httpClient, () -> spec);
    }
}
