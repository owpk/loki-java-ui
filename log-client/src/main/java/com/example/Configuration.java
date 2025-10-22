package com.example;

import java.time.Instant;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;

import com.example.logclient.LokiRestClient;
import com.example.logclient.dsl.LokiQueryExpression;
import com.example.logclient.dsl.LokiQueryExpression.LabelMatcher;
import com.example.logclient.dsl.LokiQueryRangeRequest;
import com.example.logclient.dsl.LokiQueryRequest;
import com.example.logclient.model.LogEntry;
import com.example.logclient.model.LokiRawQueryResponse;
import com.example.model.LogFilterStreamRequest;
import com.example.service.LogBackendService;

import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.WebsocketClientSpec;

@org.springframework.context.annotation.Configuration
public class Configuration {

    // @Bean
    ApplicationRunner stream(LogBackendService service) {
        return args -> {
            service.streamLogs("mock-service", 2, LogFilterStreamRequest
                    .builder()
                    .build())
                .doOnNext(it -> System.out.println(it))
                .subscribe();
        };
    }

    // @Bean
    ApplicationRunner sum(LokiRestClient lokiRestClient) {
        return args -> {
            var req = LokiQueryRequest.builder()
                    .queryExpression(queryBuilder -> queryBuilder
                            .labelQuery(l -> l.label("job", "mock-service"))
                            .query("[1h]")
                            .pipe().query("json")
                            .wrapFn(LokiQueryExpression.Fn.COUNTER_OVER_TIME)
                            .wrapFn(LokiQueryExpression.Fn.SUM)
                            .log())
                    .build();

            lokiRestClient.query(req).doOnNext(it -> System.out.println(it))
                    .subscribe();
        };
    }

    // @Bean
    ApplicationRunner expr(LokiRestClient lokiRestClient) {
        Instant.now();
        return args -> {
            var rangeRequest = LokiQueryRangeRequest.builder()
                    .queryExpression(lokiExpression -> lokiExpression
                            .labelQuery(labelQuery -> labelQuery
                                    .label("job", "mock-service")
                                    .label("message", LabelMatcher.REG_EQ, ".*timeout.*"))
                            // .label("level", "INFO"))
                            .pipe().query("json")
                            .log())
                    .limit(50)
                    // .end(Instant.ofEpochMilli(1760726370553986880L))
                    .build();

            lokiRestClient.queryRange(rangeRequest, 
                new ParameterizedTypeReference<LokiRawQueryResponse<LogEntry>>() {})
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
