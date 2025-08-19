package com.LogManagementSystem.LogManager.ParserPipeline.LogTypes;

import com.LogManagementSystem.LogManager.Entity.LogEvent;
import com.LogManagementSystem.LogManager.ParserPipeline.BindFields;
import com.LogManagementSystem.LogManager.ParserPipeline.LogEnrichment.LogEventEnrichment;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LogFmtParser implements Log {

    private LogEventEnrichment logEventEnrichment;
    private BindFields bindFields;
    private final Pattern LOGFMT_PATTERN =
            Pattern.compile("([a-zA-Z0-9_\\-]+)=((\"[^\"]*\")|([^\\s]+))");


    public LogFmtParser(LogEventEnrichment logEventEnrichment, BindFields bindFields) {
        this.logEventEnrichment = logEventEnrichment;
        this.bindFields = bindFields;
    }


    @Override
    public LogEvent parse(String log, LogEvent logEvent) {
        Map<String, Object> pairs = new HashMap<>();
        Matcher matcher = LOGFMT_PATTERN.matcher(log);
        while(matcher.find()){
            String key = matcher.group(1);
            String value = matcher.group(2);
            if(value.startsWith("\"") && value.endsWith("\"")){
                value = value.substring(1, value.length() - 1);
            }
            pairs.put(key, value);
        }
        logEvent.setAttrs(pairs);
        bindFields.bindRemainingFields(pairs, logEvent);

        return logEventEnrichment.enrichLog(logEvent);
    }
}
