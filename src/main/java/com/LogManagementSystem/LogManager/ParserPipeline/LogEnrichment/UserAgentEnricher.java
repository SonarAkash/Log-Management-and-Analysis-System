package com.LogManagementSystem.LogManager.ParserPipeline.LogEnrichment;

import org.springframework.stereotype.Service;

import com.LogManagementSystem.LogManager.Entity.LogEvent;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;

@Service
public class UserAgentEnricher {

    private final UserAgentAnalyzer analyzer;

    public UserAgentEnricher() {
        // Initialize the analyzer. It's thread-safe and should be created once.
        this.analyzer = UserAgentAnalyzer.newBuilder()
                .hideMatcherLoadStats()
                .withCache(50000)  // Increased cache size
                .withField("DeviceClass")
                .withField("DeviceBrand")
                .withField("DeviceName")
                .withField("OperatingSystemName")
                .withField("OperatingSystemVersion")
                .withField("OperatingSystemCpuBits")
                .withField("AgentName")
                .withField("AgentVersion")
                .withField("AgentClass")
                .withField("LayoutEngineName")
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

            // Device information
            logEvent.getAttrs().put("ua_device_class", agent.getValue("DeviceClass"));
            logEvent.getAttrs().put("ua_device_brand", agent.getValue("DeviceBrand"));
            logEvent.getAttrs().put("ua_device_name", agent.getValue("DeviceName"));
            
            // Operating System details
            logEvent.getAttrs().put("ua_os_name", agent.getValue("OperatingSystemName"));
            logEvent.getAttrs().put("ua_os_version", agent.getValue("OperatingSystemVersion"));
            
            // Browser information
            logEvent.getAttrs().put("ua_browser_name", agent.getValue("AgentName"));
            logEvent.getAttrs().put("ua_browser_version", agent.getValue("AgentVersion"));
            logEvent.getAttrs().put("ua_browser_engine", agent.getValue("LayoutEngineName"));
            
            // Additional categorization
            logEvent.getAttrs().put("ua_device_cpu", agent.getValue("OperatingSystemCpuBits"));
            logEvent.getAttrs().put("ua_agent_category", agent.getValue("AgentClass"));
//            System.out.println("device type : " + agent.getValue(UserAgent.DEVICE_CLASS));
//            System.out.println("os name : " + agent.getValue(UserAgent.OPERATING_SYSTEM_NAME));
//            System.out.println("agent name : " + agent.getValue(UserAgent.DEVICE_CLASS));

        } catch (Exception e) {
            // Again, loging the error but not letting it stop the processing pipeline.
            System.err.println("User-Agent parsing failed. Error: " + e.getMessage());
        }
    }
}
