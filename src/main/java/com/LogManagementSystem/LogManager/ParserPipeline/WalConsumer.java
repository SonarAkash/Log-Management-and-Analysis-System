package com.LogManagementSystem.LogManager.ParserPipeline;

import com.LogManagementSystem.LogManager.Entity.LogEvent;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Service
public class WalConsumer {
    private WalProducer producer;
    private ExecutorCompletionService<LogEvent> completionService;
    private ParserDecider parserDecider;

    public WalConsumer(WalProducer producer, ParserDecider parserDecider){
        this.producer = producer;
        this.parserDecider = parserDecider;
        completionService = new ExecutorCompletionService<LogEvent>(Executors.newFixedThreadPool(4));
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
            return parserDecider.decideAndParseLog(log);
        }
    }

    @PostConstruct
    private void init(){
        Thread startLookingForLogs = new Thread(this::lookForLogs, "Log Look up Thread");
        Thread startStoringLogs = new Thread(this::storeLogs, "Store Log Thread");
        startLookingForLogs.setDaemon(true);;
        startStoringLogs.setDaemon(true);
        startLookingForLogs.start();
        startStoringLogs.start();
    }

    private void lookForLogs(){
        while(true){
            String log = producer.getLog();
            if(log != null){
                completionService.submit(new ReadLogs(log));
            }
        }
    }
    private void storeLogs(){
        while(true){
            try {
                Future<LogEvent> completedFuture = completionService.take();
                System.out.println(completedFuture.get().toString());
//              store this log later
            } catch (InterruptedException e) {
                System.out.println("completion service was interrupted !!");
//                throw new RuntimeException(e);
            } catch (ExecutionException e) {
//                throw new RuntimeException(e);
                System.out.println("thread task was interrupted or threw exception !");
            }
        }
    }
}
