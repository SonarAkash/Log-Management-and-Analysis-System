package com.LogManagementSystem.LogManager.ParserPipeline;

import com.LogManagementSystem.LogManager.Entity.WalProperties;
import com.LogManagementSystem.LogManager.IngestGateway.FileWalAppender;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class WalProducer {


    private static final Logger logger = LoggerFactory.getLogger(FileWalAppender.class);

//    private String archivedWalDirectoryPath;
    private Path archivedWalDirPath;
//    private WatchService watchService;
    @Getter
    private final BlockingQueue<String> queue;

    @Autowired
    public WalProducer(WalProperties pros) throws IOException {
        logger.info("Producer init");
//        this.archivedWalDirectoryPath = pros.getArchivedWalDirectoryPath();
        this.archivedWalDirPath = Paths.get(pros.getArchivedWalDirectoryPath());
        if (!Files.exists(archivedWalDirPath)){
            Files.createDirectories(archivedWalDirPath);
//            System.out.println("successfully created " + archivedWalDirPath.toString());
        }
//        this.watchService = FileSystems.getDefault().newWatchService();
//        this.archivedWalDirPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
        this.queue = new ArrayBlockingQueue<String>(10000);
//        start();
//        calling the start here will be done by spring. Spring first calls constructors to initialize obj, then inject
//        inject dependencies. Calling the start() from constructor will make main thread to loop in watch service
//        to detect and wait for event. And thus main thread never actually starts which cause the application not to
//        even start. Better to annotate the start() with PostConstruct, PostConstruct is called after constructor and
//        dependency injection, letting the main thread to start the application
    }

    @PostConstruct
    public void start(){
        Thread readPendingLog = new Thread(this::processExistingFiles, "Pending Log Reader thread");
        readPendingLog.start();
        Thread walWatcherThread = new Thread(this::processNewFilesContinuously, "walWatcherThread");
        walWatcherThread.setDaemon(true);
        walWatcherThread.start();
    }

    private void processNewFilesContinuously() {

        logger.info("Starting WAL Polling thread...");
        while (true) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(archivedWalDirPath)) {

                for (Path file : stream) {
                    processFile(file);
                }
            } catch (IOException e) {
                logger.error("Error polling directory: " , e);
            }

            try {
                Thread.sleep(100); // Poll every 100ms
            } catch (InterruptedException e) {
                logger.error("Polling was interrupted !!", e);
                Thread.currentThread().interrupt();
                return;
            }
        }
//        while(true){
//            WatchKey key;
//            try {
//                key = watchService.take();
//            } catch (InterruptedException e) {
//                System.err.println("Monitoring was interrupted !!");
//                Thread.currentThread().interrupt();
//                return;
//            }
//            for(WatchEvent<?> event : key.pollEvents()){
//                WatchEvent.Kind<?> kind = event.kind();
//                if(kind == StandardWatchEventKinds.ENTRY_CREATE){
//                    @SuppressWarnings("unchecked")
//                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
//                    Path fileName = ev.context();
//                    System.err.println("processing files");
//                    Path filePath = archivedWalDirPath.resolve(fileName);
//                    processFile(filePath);
//                }
//            }
//
//            boolean valid = key.reset();
//            if (!valid) {
//                System.err.println("Directory is no longer accessible. Stopping monitor.");
//                break;
//            }
//
//        }
    }

    private void processExistingFiles() {
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(archivedWalDirPath)){
            for(Path entry : stream){
                if(Files.isRegularFile(entry)){
                    processFile(entry);
                }
            }
        } catch (IOException e){
            logger.error("Error reading existing WALs:", e);
        }
    }

    private void processFile(Path file){
        try(

                DataInputStream dis = new DataInputStream(
                        new BufferedInputStream(
                                Files.newInputStream(file)
                        )
                )
        ) {

            try{
                while(true){
                    int logSize = dis.readInt();
                    byte[] log = new byte[logSize];
                    dis.readFully(log);
                    String logMessage = new String(log, StandardCharsets.UTF_8);

                    queue.put(logMessage);
                }
            } catch (EOFException e){
//              reached end of file
//                break;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Producer interrupted !!", e);
            }
            logger.info("Wal deleted : " + file.toString());
            Files.delete(file);
        } catch (IOException e) {
            logger.error("Failed to open the Wal :(");
        }
    }

}
