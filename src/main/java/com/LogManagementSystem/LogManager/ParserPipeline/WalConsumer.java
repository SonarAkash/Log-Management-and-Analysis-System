package com.LogManagementSystem.LogManager.ParserPipeline;

import com.LogManagementSystem.LogManager.Entity.LogEvent;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Service
public class WalConsumer {
    private WalProducer producer;
    private ExecutorCompletionService<LogEvent> completionService;

    public WalConsumer(WalProducer producer){
        this.producer = producer;
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
            return new LogEvent();
        }
    }

    @PostConstruct
    private void init(){

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
//              store this log later
            } catch (InterruptedException e) {
                System.out.println("completion service was interrupted !!");
//                throw new RuntimeException(e);
            }
        }
    }
}
