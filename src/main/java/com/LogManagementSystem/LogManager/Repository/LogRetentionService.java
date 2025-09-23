package com.LogManagementSystem.LogManager.Repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;


@Service
public class LogRetentionService {

    private static final Logger logger = LoggerFactory.getLogger(LogRetentionService.class);
    private final LogEventRepository logEventRepository;

    private static final int RETENTION_DAYS = 7;

    @Autowired
    public LogRetentionService(LogEventRepository logEventRepository){
        this.logEventRepository = logEventRepository;
    }

    @Scheduled(cron = "0 0 2 * * *") // Runs at 2 AM daily
    public void purgeOldLogs(){
        Instant cutoff = Instant.now().minus(RETENTION_DAYS, ChronoUnit.DAYS);
        logger.info("Starting log purge. Deleting logs older than {}.", cutoff);
        try {
            long deletedCount = logEventRepository.deleteByIngestedAtBefore(cutoff);
            logger.info("Log purge complete. Deleted {} old log records.", deletedCount);
        } catch (Exception e) {
            logger.error("Error during scheduled log purge.", e);
        }
    }
}
