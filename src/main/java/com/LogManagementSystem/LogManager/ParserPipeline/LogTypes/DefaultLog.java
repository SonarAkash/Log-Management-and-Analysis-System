package com.LogManagementSystem.LogManager.ParserPipeline.LogTypes;

import com.LogManagementSystem.LogManager.Entity.LogEvent;
import com.LogManagementSystem.LogManager.ParserPipeline.LogEnrichment.LogEventEnrichment;
import org.springframework.stereotype.Service;

import java.time.Instant;
@Service
public class DefaultLog implements Log{
    private LogEventEnrichment logEventEnrichment;

    public DefaultLog(LogEventEnrichment logEventEnrichment) {
        this.logEventEnrichment = logEventEnrichment;
    }

    @Override
    public LogEvent parse(String log, LogEvent logEvent) {
        logEvent.setMessage(log);
        logEvent.setIngestedAt(Instant.now());
        logEventEnrichment.enrichLog(logEvent);
        return logEvent;
    }
}
