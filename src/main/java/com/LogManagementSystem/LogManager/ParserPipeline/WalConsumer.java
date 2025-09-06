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
    private BlockingQueue<String> queue;
    private BlockingQueue<LogEvent> logBuffer;

    public WalConsumer(WalProducer producer, ParserDecider parserDecider, WalProducer walProducer, DatabaseWriter databaseWriter) throws InterruptedException {
        this.producer = producer;
        this.parserDecider = parserDecider;
        completionService = new ExecutorCompletionService<>(Executors.newFixedThreadPool(4));
        this.queue = walProducer.getQueue();
        this.logBuffer = databaseWriter.getLogBuffer();
    }

    class ReadLogs implements Callable<LogEvent> {

        private String log;

        public ReadLogs(String log){
            this.log = log;
        }

        @Override
        public LogEvent call() {

            LogEvent logEvent = null;
            try{
                logEvent = parserDecider.decideAndParseLog(log);
//                Thread.sleep(500);
            } catch (Exception e) {
                System.err.println("failed parsing : " + e.getMessage());
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
//            String log = producer.getLog();
//            if(log != null){
//                completionService.submit(new ReadLogs(log));
//            }else{
//                try {
//                    Thread.sleep(3000);
//                } catch (InterruptedException e) {
////                    throw new RuntimeException(e);
//                    System.out.println("Look up thread was interrupted => cause : " + e.getMessage());
//                }
//            }
            try {
                String log = queue.take();
                completionService.submit(new ReadLogs((log)));
            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
                Thread.currentThread().interrupt();
                System.err.println("Consumer stopped !!");
            }

        }
    }
    private void storeLogs(){
        while(true){
            try {
                Future<LogEvent> completedFuture = completionService.take();
                LogEvent logEvent = completedFuture.get();
                logEvent.setIngestedAt(Instant.now());
                logBuffer.put(logEvent);
            } catch (InterruptedException e) {
                System.err.println("completion service was interrupted !!\n");
//                throw new RuntimeException(e);
                Thread.currentThread().interrupt();
                System.err.println(e.getMessage() + "\n");

            } catch (ExecutionException e) {
//                throw new RuntimeException(e);
                System.err.println("thread task was interrupted or threw exception !");
                System.err.println(e.getMessage());
            }
        }
    }
}
