package com.LogManagementSystem.LogManager.Entity;

import java.time.Instant;

public class DefaultLog implements Log{
    @Override
    public LogEvent parse(String log, LogEvent logEvent) {
        logEvent.setMessage(log);
        logEvent.setIngestedAt(Instant.now());
        return logEvent;
    }
}
