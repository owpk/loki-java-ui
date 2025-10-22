package com.example.service;

import com.example.model.BackendLog;
import com.example.model.LogFilterRequest;
import com.example.model.LogFilterStreamRequest;

import reactor.core.publisher.Flux;

public interface LogBackendService {

    boolean putLog(BackendLog log);

    Flux<BackendLog> getBackendLogs(int size, String timeRange, String order, String app);

    Flux<BackendLog> getLogs(String app, LogFilterRequest filter);

    Flux<BackendLog> streamLogs(String app, int delaySec, LogFilterStreamRequest filter);

    // Not supported now
    // AppLogNavigable getBackendLogById(String app, Long id);

    // Not supported now
    // int deleteLog(int storageDuration);

    // Not supported now
    // int deleteLogs(int storageDuration);
}