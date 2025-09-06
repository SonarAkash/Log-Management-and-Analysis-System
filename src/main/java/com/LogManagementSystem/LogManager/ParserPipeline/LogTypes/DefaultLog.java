package com.LogManagementSystem.LogManager.ParserPipeline.LogTypes;

import com.LogManagementSystem.LogManager.Entity.LogEvent;
import org.springframework.stereotype.Service;

import java.time.Instant;
@Service
public class DefaultLog implements Log{

    @Override
    public LogEvent parse(String log, LogEvent logEvent) {
        logEvent.setMessage(log);
        return logEvent;
    }
}
