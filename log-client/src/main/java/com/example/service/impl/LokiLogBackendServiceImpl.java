package com.example.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.example.logclient.LokiRestClient;
import com.example.logclient.dsl.LokiQueryExpression.LabelMatcher;
import com.example.logclient.dsl.LokiQueryExpression.LabelQueryBuilder;
import com.example.logclient.dsl.LokiQueryRangeRequest;
import com.example.logclient.dsl.LokiTailRequest;
import com.example.logclient.model.LogEntry;
import com.example.logclient.model.LokiRawQueryResponse;
import com.example.logclient.model.LokiTailResponse;
import com.example.model.BackendLog;
import com.example.model.LogFilterRequest;
import com.example.model.LogFilterStreamRequest;
import com.example.model.LogQueryFilter;
import com.example.service.LogBackendService;
import com.example.service.LokiQuerySupported;
import com.fasterxml.jackson.core.type.TypeReference;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@Service
public class LokiLogBackendServiceImpl implements LogBackendService, LokiQuerySupported<Object> {

    private final LokiRestClient lokiRestClient;
    private Map<String, String> orderMap = Map.of("ASC", "forward", "DESC", "backward");

    @Override
    public Flux<BackendLog> getBackendLogs(int size, String timeRange, String order, String app) {
        return lokiRestClient.queryRange(req -> req
                .direction(orderMap.get(order))
                .queryExpression(expr -> expr
                        .labelQuery(lableQuery -> lableQuery
                                .label("app", app))
                        .pipe().query("json")
                        .log())
                .since(timeRange)
                .limit(size),
                new ParameterizedTypeReference<LokiRawQueryResponse<LogEntry>>() {
                }).flatMapIterable(it -> it.getData().getResult())
                .map(it -> it.getStream())
                .map(this::mapToBackendLog);
    }

    @Override
    public boolean putLog(BackendLog log) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'putError'");
    }

    @Override
    public Flux<BackendLog> getLogs(String app, LogFilterRequest filter) {
        var req = mapToQueryRangeRequest(app, filter);

        return lokiRestClient.queryRange(req,
                new ParameterizedTypeReference<LokiRawQueryResponse<LogEntry>>() {
                }).flatMapIterable(it -> it.getData().getResult())
                .map(it -> it.getStream())
                .map(this::mapToBackendLog);
    }

    @Override
    public Flux<BackendLog> streamLogs(String app, int delaySec, @NonNull LogFilterStreamRequest filter) {
        var labelQuery = toLabelQuery(filter.getFilters());

        var lokiTailRequest = LokiTailRequest.builder()
                .delayFor(delaySec)
                .limit(filter.getLimit())
                .queryExpression(expr -> expr.labelQuery(labelQuery.label("app", app))
                        .pipe().query("json")
                        .log());

        if (filter.getStart() != null)
            lokiTailRequest.start(Instant.ofEpochMilli(filter.getStart()));

        return lokiRestClient.tailLogsStream(lokiTailRequest.build(), 
            new TypeReference<LokiTailResponse<LogEntry>>() {})
                .flatMapIterable(it -> it.streams())
                .map(it -> it.stream())
                .map(this::mapToBackendLog);
    }

    private LokiQueryRangeRequest mapToQueryRangeRequest(String app, LogFilterRequest filter) {
        var tr = filter.getTimeRange();
        var size = filter.getLimit();
        var order = filter.getOrder();
        var end = filter.getEnd();
        var start = filter.getStart();

        var labelQuery = toLabelQuery(filter.getFilters());

        return LokiQueryRangeRequest.builder()
                .direction(orderMap.get(order))
                .queryExpression(expr -> expr
                        .labelQuery(labelQuery.label("app", app))
                        .pipe().query("json"))
                .since(tr)
                .end(end)
                .start(start)
                .limit(size)
                .build();
    }

    private LabelQueryBuilder toLabelQuery(List<LogQueryFilter> filter) {
        var labelQuery = new LabelQueryBuilder();
        if (filter != null)
            filter.stream()
                    .filter(Objects::nonNull)
                    .filter(it -> it.getValue() != null)
                    .forEach(f -> labelQuery.label(f.getField(), LabelMatcher.of(f.getOperator()), f.getValue()));
        return labelQuery;
    }

    @Override
    public Flux<Object> rangeRequest(LokiQueryRangeRequest rangeRequest) {
        return lokiRestClient.queryRange(rangeRequest,
                new ParameterizedTypeReference<LokiRawQueryResponse<LogEntry>>() {
                }).flatMapIterable(it -> it.getData().getResult());
    }

    private BackendLog mapToBackendLog(LogEntry logEntry) {
        return BackendLog.builder()
                .logTs(logEntry.timestamp())
                .logMessage(logEntry.message())
                .logger(logEntry.logger())
                .logThread(logEntry.thread())
                .logLevel(logEntry.level()).build();
    }

}
