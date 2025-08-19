package com.LogManagementSystem.LogManager.ParserPipeline.LogTypes;

import com.LogManagementSystem.LogManager.Entity.LogEvent;
import com.LogManagementSystem.LogManager.ParserPipeline.BindFields;
import com.LogManagementSystem.LogManager.ParserPipeline.LogEnrichment.LogEventEnrichment;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LogFmtParser implements Log {

    private LogEventEnrichment logEventEnrichment;
    private BindFields bindFields;


    public LogFmtParser(LogEventEnrichment logEventEnrichment, BindFields bindFields) {
        this.logEventEnrichment = logEventEnrichment;
        this.bindFields = bindFields;
    }


    @Override
    public LogEvent parse(String log, LogEvent logEvent) {
        String[] logs = log.split("\\s");
        Map<String, Object> pairs = new HashMap<>();
        for (String s : logs) {
            if(s.trim().isEmpty()) continue;
            int equalIdx = s.indexOf("=");
            String key = s.substring(0, equalIdx);
            Object value = s.substring(equalIdx + 1);
            pairs.put(key, value);
        }
        logEvent.setAttrs(pairs);
        bindFields.bindRemainingFields(pairs, logEvent);
        return logEventEnrichment.enrichLog(logEvent);
    }
}
