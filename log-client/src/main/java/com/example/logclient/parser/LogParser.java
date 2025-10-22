package com.example.logclient.parser;

import com.example.logclient.model.LogEntry;

public interface LogParser {
    LogEntry parseLogLine(String log);
}
