package com.example.logclient.model;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record LogEntry(
        @JsonProperty(value = "_timestamp")
        ZonedDateTime timestamp,
        String job,
        String level,
        String logger,
        String service,
        String message,
        String thread
) {}
