package com.LogManagementSystem.LogManager.ParserPipeline;

import com.LogManagementSystem.LogManager.Entity.LogEvent;
import com.LogManagementSystem.LogManager.ParserPipeline.Repository.LogEventRepository;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Service
public class DatabaseWriter {
    @Getter
    private final BlockingQueue<LogEvent> logBuffer;
    private final LogEventRepository logEventRepository;

    @Autowired
    public DatabaseWriter(BlockingQueue<LogEvent> logBuffer, LogEventRepository logEventRepository) {
        this.logBuffer = logBuffer;
        this.logEventRepository = logEventRepository;
    }

    private static final int BATCH_SIZE = 500;
    private static final int POLL_TIMEOUT_MS = 2000;

    @PostConstruct
    private void start(){
        Thread writer = new Thread(this::run);
        writer.setDaemon(true);
        writer.setName("database writer");
        writer.start();
    }

    private void run(){
        List<LogEvent> batch = new ArrayList<>(BATCH_SIZE);
        while(!Thread.currentThread().isInterrupted()){
            try {
                LogEvent logEvent = logBuffer.poll(POLL_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                if(logEvent != null){
                    batch.add(logEvent);
                    logBuffer.drainTo(batch, BATCH_SIZE - 1);
                }
                if(!batch.isEmpty()){
                    logEventRepository.saveAll(batch);
                    batch.clear();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("!!! FAILED TO SAVE LOG TO DATABASE !!!");
                e.printStackTrace();
            }
        }
    }
}
