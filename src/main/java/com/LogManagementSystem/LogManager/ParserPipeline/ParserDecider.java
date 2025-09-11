package com.LogManagementSystem.LogManager.ParserPipeline;

import com.LogManagementSystem.LogManager.Entity.LogEvent;
import com.LogManagementSystem.LogManager.ParserPipeline.LogTypes.DefaultLog;
import com.LogManagementSystem.LogManager.ParserPipeline.LogTypes.JsonParser;
import com.LogManagementSystem.LogManager.ParserPipeline.LogTypes.LogFmtParser;
import com.LogManagementSystem.LogManager.ParserPipeline.LogTypes.LogType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class ParserDecider {

    private final ObjectMapper mapper;
    private final Pattern LOGFMT_PATTERN;
    private final JsonParser jsonParser;
    private final LogFmtParser logFmtParser;
    private final DefaultLog defaultLog;

    public ParserDecider(JsonParser jsonParser
            , LogFmtParser logFmtParser, DefaultLog defaultLog){
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

        this.jsonParser = jsonParser;
        this.logFmtParser = logFmtParser;
        this.defaultLog = defaultLog;
    }

    public LogEvent decideAndParseLog(String log){
        LogEvent logEvent = new LogEvent();

        try{
            int start = log.indexOf("["), end = log.indexOf("]");
            UUID tenantId = UUID.fromString(log.substring(start + 1, end));
            log = log.substring(end + 1);
            start = log.indexOf("[");
            end = log.indexOf("]");
            Instant timestamp = Instant.parse(log.substring(start + 1, end)); // when the log received by the controller
            log = log.substring(end + 1); // now the log only contains raw log
            logEvent.setTenantId(tenantId);
            logEvent.setTs(timestamp);
        } catch (Exception e) {
            System.err.println("something went wrong while parsing :( , method : decideAndParseLog -> " + e.getMessage());
        }

        // switch case to identify the type of log i.e. Json or logfmt for now only

        LogType logType = determineLogType(log.trim());
        try {
            logEvent =  switch (logType){
                case LogType.JSON -> jsonParser.parse(log, logEvent);
                case LogType.LOGFMT -> logFmtParser.parse(log, logEvent);
                default -> defaultLog.parse(log, logEvent);
            };
        } catch (Exception e) {
            System.err.println("TYPE parsing failed " + e.getMessage());
        }

        return logEvent;
    }

    public LogType determineLogType(String log) {
        return checkJSON(log);
    }

    private LogType checkJSON(String log) {
        try {
            mapper.readTree(log);
            return LogType.JSON;
        } catch (Exception e) {
            return checkLogfmt(log);
        }
    }

    private LogType checkLogfmt(String log) {
        return LOGFMT_PATTERN.matcher(log).find() ? LogType.LOGFMT : LogType.DEFAULT;
    }
}
