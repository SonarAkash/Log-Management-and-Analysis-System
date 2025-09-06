package com.LogManagementSystem.LogManager.ParserPipeline.LogEnrichment;

import com.LogManagementSystem.LogManager.Entity.LogEvent;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.model.CityResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;

@Service
public class GeoIpEnricher {

    private DatabaseReader dbReader;
    private final ResourceLoader resourceLoader;

    public GeoIpEnricher(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() throws IOException {
        // Load the GeoLite2 database from the classpath resources
//        File database = ResourceUtils.getFile("classpath:GeoLite2-City.mmdb");
//        dbReader = new DatabaseReader.Builder(database).build();

        /*
        * The above commented code works in dev environment(like IDE) because the .mmdb file
        * is in the file system and therefore using ResourceUtils.getFile("classpath:GeoLite2-City.mmdb");
        * works, but when the project is build into jar file for deployment
        * all the resources inside the resources folder get packaged inside
        * it as well so they are no longer part of the file system and therefore
        * the ResourceUtils.getFile("classpath:GeoLite2-City.mmdb"); fails
        * because it is no longer an individual file on the filesystem. The getFile() method fails because there is
        *  no filesystem path to a file that is zipped inside another file.
        *
        *
        * The correct way to access a resource that might be inside a JAR is to read it as a stream of
        *  data (InputStream), not as a File. The MaxMind DatabaseReader is designed to be initialized
        *  directly from an InputStream for this exact reason.
        * */

        // Use Spring's ResourceLoader to get a handle to the resource
        Resource resource = resourceLoader.getResource("classpath:GeoLite2-City.mmdb");

        // Use a try-with-resources block to safely open and close the InputStream
        try (InputStream dbStream = resource.getInputStream()) {
            // Build the DatabaseReader from the stream of data
            dbReader = new DatabaseReader.Builder(dbStream).build();
        }
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
