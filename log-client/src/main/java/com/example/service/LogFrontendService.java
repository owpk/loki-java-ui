package com.example.service;

import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;

import com.example.logclient.model.PageResponse;
import com.example.model.FrontendLog;

public interface LogFrontendService {

    boolean putLog(FrontendLog log);

    PageResponse<FrontendLog> getLogsFrontend(Pageable pageable);

    // ? id это что
    FrontendLog getFrontendLog(Long id);

    // FrontendLogNavigable getFrontendLogNavigable(Long id);

    // Not supported now
    // int deleteLog(int storageDuration);

    // Not supported now
    // int deleteLogs(int storageDuration);
}
