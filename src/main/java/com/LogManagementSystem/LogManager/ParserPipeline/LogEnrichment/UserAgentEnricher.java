package com.LogManagementSystem.LogManager.ParserPipeline.LogEnrichment;

import com.LogManagementSystem.LogManager.Entity.LogEvent;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.stereotype.Service;

@Service
public class UserAgentEnricher {

    private final UserAgentAnalyzer analyzer;

    public UserAgentEnricher() {
        // Initialize the analyzer. It's thread-safe and should be created once.
        this.analyzer = UserAgentAnalyzer.newBuilder()
                .hideMatcherLoadStats()
                .withCache(10000)
                .build();
    }

    public void enrich(LogEvent logEvent) {
        Object userAgentStringObj = logEvent.getAttrs().get("http_user_agent");

        if (userAgentStringObj == null || !(userAgentStringObj instanceof String)) {
            return;
        }

        try {
            String userAgentString = (String) userAgentStringObj;
            UserAgent agent = analyzer.parse(userAgentString);

            logEvent.getAttrs().put("ua_device_class", agent.getValue(UserAgent.DEVICE_CLASS)); // e.g., Desktop, Mobile, Tablet
            logEvent.getAttrs().put("ua_os_name", agent.getValue(UserAgent.OPERATING_SYSTEM_NAME)); // e.g., Windows, Android
            logEvent.getAttrs().put("ua_agent_name", agent.getValue(UserAgent.AGENT_NAME)); // e.g., Chrome, Firefox
//            System.out.println("device type : " + agent.getValue(UserAgent.DEVICE_CLASS));
//            System.out.println("os name : " + agent.getValue(UserAgent.OPERATING_SYSTEM_NAME));
//            System.out.println("agent name : " + agent.getValue(UserAgent.DEVICE_CLASS));

        } catch (Exception e) {
            // Again, loging the error but not letting it stop the processing pipeline.
            System.err.println("User-Agent parsing failed. Error: " + e.getMessage());
        }
    }
}
