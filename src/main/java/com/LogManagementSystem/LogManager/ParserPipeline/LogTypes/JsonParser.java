package com.LogManagementSystem.LogManager.ParserPipeline.LogTypes;

import com.LogManagementSystem.LogManager.Entity.LogEvent;
import com.LogManagementSystem.LogManager.ParserPipeline.BindFields;
import com.LogManagementSystem.LogManager.ParserPipeline.LogEnrichment.LogEventEnrichment;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class JsonParser implements Log{

    private BindFields bindFields;

    private LogEventEnrichment logEventEnrichment;


    public JsonParser(LogEventEnrichment logEventEnrichment, BindFields bindFields) {
        this.logEventEnrichment = logEventEnrichment;
        this.bindFields = bindFields;
    }


    @Override
    public LogEvent parse(String log, LogEvent logEvent) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> nestedMap = mapper.readValue(log,
                    Map.class);
            Map<String, Object> flattenMap = new LinkedHashMap<>();
            flattenJSON("", flattenMap, nestedMap);
            logEvent.setAttrs(flattenMap);
            bindFields.bindRemainingFields(flattenMap, logEvent);
        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
            System.out.println("Failed parsing json !!");
        }

        return logEventEnrichment.enrichLog(logEvent);
    }


    @SuppressWarnings("unchecked")
    private void flattenJSON(String currPath, Map<String, Object> flattenMap, Object value) {
        if(value instanceof Map){
            String prefix = currPath.isEmpty() ? "" : currPath + ".";
            for(Map.Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()){
                flattenJSON(prefix + entry.getKey(), flattenMap, entry.getValue());
            }
        }else if(value instanceof List<?>){
            for(int i=0; i<((List<?>) value).size(); i++){
                flattenJSON(currPath + "[" + i + "]", flattenMap, ((List<?>) value).get(i));
            }
        }else{
            flattenMap.put(currPath, value);
        }
    }
}
