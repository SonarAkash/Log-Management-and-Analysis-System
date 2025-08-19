package com.LogManagementSystem.LogManager.ParserPipeline.LogEnrichment;

import com.LogManagementSystem.LogManager.Entity.LogEvent;
import org.springframework.stereotype.Service;

@Service
public class LogEventEnrichment {

    private GeoIpEnricher geoIpEnricher;
    private UserAgentEnricher userAgentEnricher;

    public LogEventEnrichment(GeoIpEnricher geoIpEnricher, UserAgentEnricher userAgentEnricher) {
        this.geoIpEnricher = geoIpEnricher;
        this.userAgentEnricher = userAgentEnricher;
    }

    public LogEvent enrichLog(LogEvent logEvent){
        geoIpEnricher.enrich(logEvent);
        userAgentEnricher.enrich(logEvent);
        return logEvent;
    }

}
