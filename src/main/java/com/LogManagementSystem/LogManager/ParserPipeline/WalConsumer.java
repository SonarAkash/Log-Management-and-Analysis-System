package com.LogManagementSystem.LogManager.ParserPipeline;

import com.LogManagementSystem.LogManager.Entity.LogEvent;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.*;

@Service
public class WalConsumer {
    private WalProducer producer;
    private ExecutorCompletionService<LogEvent> completionService;
    private ParserDecider parserDecider;

    public WalConsumer(WalProducer producer, ParserDecider parserDecider){
        this.producer = producer;
        this.parserDecider = parserDecider;
        completionService = new ExecutorCompletionService<>(Executors.newFixedThreadPool(4));
    }

    class ReadLogs implements Callable<LogEvent> {

        private String log;

        public ReadLogs(String log){
            this.log = log;
        }

        @Override
        public LogEvent call() {

            // call a method that decides which parser
            // to use and returns a ready to store log
            LogEvent logEvent = null;
            try{
                logEvent = parserDecider.decideAndParseLog(log);
//                Thread.sleep(500);
            } catch (Exception e) {
                System.out.println("failed parsing : " + e.getMessage());
            }

            return logEvent;
        }
    }


    @PostConstruct
    private void init(){
        Thread startLookingForLogs = new Thread(this::lookForLogs, "Log Look up Thread");
        Thread startStoringLogs = new Thread(this::storeLogs, "Store Log Thread");
        startLookingForLogs.setDaemon(true);
        startStoringLogs.setDaemon(true);
        startLookingForLogs.start();
        startStoringLogs.start();
    }

    private void lookForLogs() {
        while(true){
            String log = producer.getLog();
            if(log != null){
                completionService.submit(new ReadLogs(log));
            }else{
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
                    System.out.println("Look up thread was interrupted => cause : " + e.getMessage());
                }
            }
        }
    }
    private void storeLogs(){
        while(true){
            try {
                Future<LogEvent> completedFuture = completionService.take();
                LogEvent logEvent = completedFuture.get();
                logEvent.setIngestedAt(Instant.now());
                System.out.println(logEvent + "\n");
//              store this log later
            } catch (InterruptedException e) {
                System.out.println("completion service was interrupted !!\n");
//                throw new RuntimeException(e);
                System.out.println(e.getMessage() + "\n");

            } catch (ExecutionException e) {
//                throw new RuntimeException(e);
                System.out.println("thread task was interrupted or threw exception !");
                System.out.println(e.getMessage());
            }
        }
    }
}
