package com.LogManagementSystem.LogManager.Entity;

import org.springframework.stereotype.Component;

@Component
public interface Log {
    LogEvent parse(String log, LogEvent logEvent);
}
