package com.LogManagementSystem.LogManager.Entity;

import org.springframework.stereotype.Component;

@Component
public class JsonLog implements Log{
    @Override
    public LogEvent parse(String log, LogEvent logEvent) {
        return null;
    }
}
