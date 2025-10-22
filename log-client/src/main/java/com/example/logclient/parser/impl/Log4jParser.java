package com.example.logclient.parser.impl;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.example.logclient.model.LogEntry;
import com.example.logclient.parser.LogParser;

@Component
@Deprecated
public class Log4jParser implements LogParser {

    //@formatter:off
    private static final String LOG_PATTERN =
            "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z)\\s+" + // 1: timestamp
            "([A-Z]+)\\s+" +                                                // 2: log level
            "(\\d+)\\s+---\\s+" +                                           // 3: PID
            "\\[([^\\]]*)\\]\\s+" +                                         // 4: traceId ("demo")
            "\\[\\s*([^\\]]*)\\]\\s+" +                                     // 5: thread (" main")
            "([^\\s]+)\\s+:\\s+" +                                          // 6: logger name ("example.MockService")
            "(.*)$";
    //@formatter:on

    private static final Pattern pattern = Pattern.compile(LOG_PATTERN);

    @Override
    public LogEntry parseLogLine(String logLine) {
        var matcher = pattern.matcher(logLine);
        if (matcher.matches()) {
            return LogEntry.builder()
                    // .timestamp(matcher.group(1))
                    .level(matcher.group(2))
                    .thread(matcher.group(5))
                    .logger(matcher.group(6))
                    .message(matcher.group(7))
                    .build();
        }
        return null;
    }

}
