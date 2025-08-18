package com.LogManagementSystem.LogManager.ParserPipeline;

import com.LogManagementSystem.LogManager.Entity.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class ParserDecider {

    private ObjectMapper mapper;
    private Pattern LOGFMT_PATTERN;
//    private Log processedLog;

    public ParserDecider(Log processedLog){
//        this.processedLog = processedLog;
        mapper = new ObjectMapper();
        LOGFMT_PATTERN = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_-]*=([^\\s\"]+|\"[^\"]*\")");
        /*
        * (A | B) -> This means either A or B
        *
        * Part A → [^\\s\"]+
        * [...] → character class
        * ^ inside → negation (not these characters)
        * \\s → whitespace (space, tab, newline, etc.)
        * \" → literal double quote (")
        * + → means one or many times
        * So [^\\s\"] means:
        any character except whitespace and double-quote


        * Part B → \"[^\"]*\"

        * First \" → literal opening quote (")
        * [^\"]* → zero or more characters that are not a double-quote
        * Last \" → literal closing quote (")
        * So this matches a quoted string.

         * */
    }

    private LogEvent decideAndParseLog(String log){
        int start = log.indexOf("["), end = log.indexOf("]");
        UUID tenantId = UUID.fromString(log.substring(start + 1, end));
        log = log.substring(end + 1);
        start = log.indexOf("[");
        end = log.indexOf("]");
        Instant timestamp = Instant.parse(log.substring(start + 1, end)); // when the log received by the controller
        log = log.substring(end + 1); // now the log only contains raw log
        LogEvent logEvent = new LogEvent();
        logEvent.setTenantId(tenantId);
        logEvent.setTs(timestamp);

        // switch case to identify the type of log i.e. Json or logfmt for now only

        String logType = determineLogType(log.trim());
        logEvent =  switch (logType){
            case "JSON" -> new JsonLog().parse(log, logEvent);
            case "LOGFMT" -> new LogFmtLog().parse(log, logEvent);
            default -> new DefaultLog().parse(log, logEvent);
        };
        return logEvent;
    }

    private String determineLogType(String log) {
        return checkJSON(log);
    }

    private String checkJSON(String log) {
        try {
            mapper.readTree(log);
            return "JSON";
        } catch (Exception e) {
            return checkLogfmt(log);
        }
    }

    private String checkLogfmt(String log) {
        return LOGFMT_PATTERN.matcher(log).find() ? "LOGFMT" : "DEFAULT";
    }
}
