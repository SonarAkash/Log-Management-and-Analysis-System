package com.LogManagementSystem.LogManager.IngestGateway;

import com.LogManagementSystem.LogManager.Entity.WalProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FileWalAppender implements AutoCloseable{
    private final long maxSize;
    private long currSize;
    private Path activeWalPath;
    private Path archivedWalDirectoryPath;
    private DataOutputStream dos;
    private static final Logger logger = LoggerFactory.getLogger(FileWalAppender.class);
//    private Fille
//    private BufferedOutputStream bos;

    public FileWalAppender(WalProperties pros) throws IOException {
        this.maxSize = pros.getMaxSize();
        this.activeWalPath = Paths.get(pros.getActiveWalPath());
        Path parentDir = activeWalPath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
        this.archivedWalDirectoryPath = Paths.get(pros.getArchivedWalDirectoryPath());
        if (!Files.exists(archivedWalDirectoryPath)){
            Files.createDirectories(archivedWalDirectoryPath);
        }
//        if(Files.exists(activeWalPath)){
//            currSize = Files.size(activeWalPath);
//            dos = new DataOutputStream(new BufferedOutputStream(
//                    new FileOutputStream(pros.getActiveWalPath(), true)
//            ));
//        }else{
//            currSize = 0L;
//            dos = new DataOutputStream(new BufferedOutputStream(
//                    Files.newOutputStream(this.activeWalPath, StandardOpenOption.CREATE
//                            , StandardOpenOption.APPEND)
//            ));
//        }

        // The above comment code does the same thing as below code but
        // for consistence i found this new way of doing the same thing
        // it looks good
        currSize = Files.exists(activeWalPath) ? Files.size(activeWalPath) : 0L;
        dos = new DataOutputStream(new BufferedOutputStream(
                Files.newOutputStream(activeWalPath, StandardOpenOption.CREATE, StandardOpenOption.APPEND)
        ));
    }

    public synchronized boolean write(String log){
        byte[] logBytes = log.getBytes(StandardCharsets.UTF_8);
        int logEntrySize = 4 + logBytes.length; // 4 bytes for integer logsize
        if(currSize + logEntrySize >= maxSize){
            if(!rotate()) return false;
        }
        try {
            int size = logBytes.length;
            this.dos.writeInt(size);
            this.dos.write(logBytes);
            currSize += logEntrySize;
        } catch (IOException e) {
            logger.error("Writing to WAL failed !", e);
            return false;
        }
        return true;
    }

    @Scheduled(fixedRate = 300000)
    private synchronized boolean rotate() {
        if(currSize == 0) return false;
        try {
            this.dos.close();
            String timestamp = String.valueOf(Instant.now().toEpochMilli());
            Path newName = this.archivedWalDirectoryPath.resolve("wal-" + timestamp + ".log");
            Files.move(activeWalPath, newName);
            logger.info("WAL segment rotated. New file: {}", newName.toString());
//            Thread.sleep(5000);

            dos = new DataOutputStream(new BufferedOutputStream(
                    Files.newOutputStream(this.activeWalPath, StandardOpenOption.CREATE
                            , StandardOpenOption.TRUNCATE_EXISTING)
            ));
            currSize = 0L;
        } catch (IOException e) {
            logger.error("Rotating WAL failed !", e);
            return false;
        }
        return true;
    }

    @Override
    public void close() throws Exception { // closes the files just before the application stops
        if(dos != null){
            dos.flush();
            dos.close();
        }
    }
}
