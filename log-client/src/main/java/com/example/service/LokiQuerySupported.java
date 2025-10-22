package com.example.service;

import com.example.logclient.dsl.LokiQueryRangeRequest;

import reactor.core.publisher.Flux;

public interface LokiQuerySupported<T> {
    Flux<T> rangeRequest(LokiQueryRangeRequest rangeRequest);
}