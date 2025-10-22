package com.example.model;

import java.io.Serializable;
import java.time.ZonedDateTime;

import lombok.Data;

@Data
public class FrontendLog implements Serializable {
    private Long id;
    private String userLogin;
    private String system;
    private ZonedDateTime timestamp;
    private String location;
    private String browser;
    private String errorType;
    private String errorMessage;
    private String errorStack;
    private String version;
    private String tracking;
}
