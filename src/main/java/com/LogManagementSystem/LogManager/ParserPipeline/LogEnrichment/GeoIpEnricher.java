package com.LogManagementSystem.LogManager.ParserPipeline.LogEnrichment;

import com.LogManagementSystem.LogManager.Entity.LogEvent;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.model.CityResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

@Service
public class GeoIpEnricher {

    private DatabaseReader dbReader;

    @PostConstruct
    public void init() throws IOException {
        // Load the GeoLite2 database from the classpath resources
        File database = ResourceUtils.getFile("classpath:GeoLite2-City.mmdb");
        dbReader = new DatabaseReader.Builder(database).build();
    }

    public void enrich(LogEvent logEvent) {
        // Only proceed if there is a client IP to look up
        if (logEvent.getClientIp() == null || logEvent.getClientIp().isBlank()) {
            return;
        }

        try {
            InetAddress ipAddress = InetAddress.getByName(logEvent.getClientIp());
            CityResponse response = dbReader.city(ipAddress);

            String country = response.getCountry().getName();
            String city = response.getCity().getName();


            logEvent.getAttrs().put("country", country);
            logEvent.getAttrs().put("city", city);

        } catch (AddressNotFoundException e) {
           // address not present in db
        } catch (Exception e) {

            System.err.println("GeoIP enrichment failed for IP: " + logEvent.getClientIp() + " Error: " + e.getMessage());
        }
    }
}
