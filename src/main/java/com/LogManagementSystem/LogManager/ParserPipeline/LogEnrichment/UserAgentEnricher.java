package com.LogManagementSystem.LogManager.ParserPipeline.LogEnrichment;

import org.springframework.stereotype.Service;

import com.LogManagementSystem.LogManager.Entity.LogEvent;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;

@Service
public class UserAgentEnricher {

    private final UserAgentAnalyzer analyzer = initializeAnalyzer();

    private UserAgentAnalyzer initializeAnalyzer() {
        try {
            // Initialize with minimal configuration for container environment
            return UserAgentAnalyzer.newBuilder()
                    .hideMatcherLoadStats()
                    .withCache(1000)
                    // Core device information
                    .withField(UserAgent.DEVICE_CLASS)
                    .withField(UserAgent.DEVICE_BRAND)
                    .withField(UserAgent.DEVICE_NAME)
                    // Operating system information
                    .withField(UserAgent.OPERATING_SYSTEM_NAME)
                    .withField(UserAgent.OPERATING_SYSTEM_VERSION)
                    // Browser information
                    .withField(UserAgent.AGENT_NAME)
                    .withField(UserAgent.AGENT_VERSION)
                    .withField(UserAgent.AGENT_CLASS)
                    // Layout engine
                    .withField(UserAgent.LAYOUT_ENGINE_NAME)
                    .dropTests()  // Reduce memory usage
                    .delayInitialization() // Delay initialization until first use
                    .build();
        } catch (Exception e) {
            // Ultra minimal fallback configuration
            return UserAgentAnalyzer.newBuilder()
                    .hideMatcherLoadStats()
                    .withCache(100)
                    .withField("DeviceClass")
                    .withField("AgentName")
                    .withField("OperatingSystemName")
                    .dropTests()
                    .build();
        }
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
            // CPU information is not available in this version
            // logEvent.getAttrs().put("ua_device_cpu", agent.getValue("OperatingSystemCpuBits"));
            logEvent.getAttrs().put("ua_agent_category", agent.getValue("AgentClass"));
//            System.out.println("device type : " + agent.getValue(UserAgent.DEVICE_CLASS));
//            System.out.println("os name : " + agent.getValue(UserAgent.OPERATING_SYSTEM_NAME));
//            System.out.println("agent name : " + agent.getValue(UserAgent.DEVICE_CLASS));

        } catch (Exception e) {
            // Use a simpler logging approach and continue
            logEvent.getAttrs().put("ua_parse_error", "Failed to parse user agent: " + e.getMessage());
        }
    }

    // Method to check if the analyzer is ready
    public boolean isReady() {
        return analyzer != null;
    }
}
