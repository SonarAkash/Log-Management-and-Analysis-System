package com.LogManagementSystem.LogManager.IngestGateway;

import com.LogManagementSystem.LogManager.LogStream.ActiveClient;
import com.LogManagementSystem.LogManager.LogStream.LogMessage;
import com.LogManagementSystem.LogManager.ParserPipeline.ParserDecider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
public class IngestController {

    private final FileWalAppender fileWalAppender;
    private final ActiveClient activeClient;
    private ExecutorService executorService;
    private ParserDecider parserDecider;
    private SimpMessagingTemplate template;

    public IngestController(FileWalAppender fileWalAppender, ActiveClient activeClient, ParserDecider parserDecider, @Lazy SimpMessagingTemplate template) {
        this.fileWalAppender = fileWalAppender;
        this.activeClient = activeClient;
        this.parserDecider = parserDecider;
        this.template = template;
        executorService = Executors.newFixedThreadPool(4);
    }


    @PostMapping("api/v1/ingest")
    public ResponseEntity<?> incomingLog(HttpServletRequest request,
                                            @RequestBody String rawLog){
        String timestamp = String.valueOf(Instant.now());
        UUID tenantId =  (UUID) request.getAttribute("tenantId");
        System.out.println("received logs");
        if(activeClient.containsClient(tenantId)){
            executorService.submit(new StreamLogs(tenantId, rawLog));
        }
        rawLog = "[" + tenantId + "][" + timestamp + "]"+ rawLog;
        boolean result = fileWalAppender.write(rawLog);
        return result ? ResponseEntity.accepted().build()
                : ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    class StreamLogs implements Runnable{
        private UUID tenantId;
        private String log;
        public StreamLogs(UUID tenantId, String log){
            this.tenantId = tenantId;
            this.log = log;
        }
        @Override
        public void run() {
            String email = activeClient.getClientEmail(tenantId);
            String logType = parserDecider.determineLogType(log).name();
            String destination =  "/queue/stream/" + tenantId.toString();
            System.out.println("sending logs to  " + email + " " + tenantId);
            template.convertAndSend(
                    destination,
                    LogMessage.builder()
                            .payload(log)
                            .type(logType).build());
        }
    }
}
