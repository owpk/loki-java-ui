package com.example.model;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BackendLog {

    private String funcSystem;
    private String instance; // app
    private String appVersion; // version
    private String userLogin;

    private ZonedDateTime logTs;
    private String logLevel;
    private String mdc;
    private String logger;
    private String logThread;
    private String logMessage;

    private String logException;
    private String logStack;
}
