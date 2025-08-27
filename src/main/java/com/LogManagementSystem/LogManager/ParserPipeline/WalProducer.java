package com.LogManagementSystem.LogManager.ParserPipeline;

import com.LogManagementSystem.LogManager.Entity.WalProperties;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
//@ConfigurationProperties(prefix = "wal")
public class WalProducer {

    private String archivedWalDirectoryPath;
    private Path archivedWalDirPath;
    private WatchService watchService;
    @Getter
    private BlockingQueue<String> queue;

    public WalProducer(WalProperties pros) throws IOException {
        this.archivedWalDirectoryPath = pros.getArchivedWalDirectoryPath();
        this.archivedWalDirPath = Paths.get(this.archivedWalDirectoryPath);
        this.watchService = FileSystems.getDefault().newWatchService();
        this.archivedWalDirPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
        this.queue = new ArrayBlockingQueue<String>(10000);
//        start();
//        calling the start here will be done by spring. Spring first calls constructors to initialize obj, then inject
//        inject dependencies. Calling the start() from constructor will make main thread to loop in watch service
//        to detect and wait for event. And thus main thread never actually starts which cause the application no to
//        even start. Better to annotate the start() with PostConstruct, PostConstruct is called after constructor and
//        dependency injection, letting the main thread to start the application
    }

    @PostConstruct
    public void start(){
        Thread readPendingLog = new Thread(this::processExistingFiles, "Pending Log Reader thread");
        readPendingLog.start();
//        processExistingFiles();
        Thread walWatcherThread = new Thread(this::processNewFilesContinuously, "walWatcherThread");
        walWatcherThread.setDaemon(true);
        walWatcherThread.start();
    }

    private void processNewFilesContinuously() {
        while(true){
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException e) {
                System.out.println("Monitoring was interrupted !!");
                Thread.currentThread().interrupt();
                return;
            }
            for(WatchEvent<?> event : key.pollEvents()){
                WatchEvent.Kind<?> kind = event.kind();
                if(kind == StandardWatchEventKinds.ENTRY_CREATE){
                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path fileName = ev.context();

                    Path filePath = archivedWalDirPath.resolve(fileName);
                    processFile(filePath);
                }
            }

            boolean valid = key.reset();
            if (!valid) {
                System.err.println("Directory is no longer accessible. Stopping monitor.");
                break;
            }

        }
    }

    private void processExistingFiles() {
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(archivedWalDirPath)){
            for(Path entry : stream){
                if(Files.isRegularFile(entry)){
                    processFile(entry);
                }
            }
        } catch (IOException e){
            System.out.println("Error reading existing files:" + e.getMessage());
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

//            System.out.println("file opend");
//            System.out.println(Thread.currentThread().getName());
//            Thread.sleep(5000);

            try{
                while(true){
                    int logSize = dis.readInt();
                    byte[] log = new byte[logSize];
                    dis.readFully(log);
                    String logMessage = new String(log, StandardCharsets.UTF_8);

                    queue.put(logMessage);
//                    System.out.println("queued : " + logMessage + " : " + queue.size());
                }
            } catch (EOFException e){
//              reached end of file
//                break;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("producer interrupted !!");
                throw new RuntimeException(e);
            }
            System.out.println("file created : " + file.toString());
            Files.delete(file);
        } catch (IOException e) {
            System.out.println("failed to open the file :(");
//            throw new RuntimeException(e);
        }
    }

}
