package com.LogManagementSystem.LogManager.IngestGateway;

import com.LogManagementSystem.LogManager.Entity.WalProperties;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

@Service
public class FileWalAppender implements  AutoCloseable{
    private long maxSize;
    private long currSize;
    private Path activeWalPath;
    private Path archivedWalDirectoryPath;
    private BufferedOutputStream bos;

    public FileWalAppender(WalProperties pros) throws IOException {
        this.maxSize = pros.getMaxSize();
        this.currSize = 0L;
        this.activeWalPath = Paths.get(pros.getActiveWalPath());
        this.archivedWalDirectoryPath = Paths.get(pros.getArchivedWalDirectoryPath());
        this.bos = new BufferedOutputStream(
            Files.newOutputStream(this.activeWalPath, StandardOpenOption.CREATE
                    , StandardOpenOption.CREATE)
        );
    }

    public synchronized boolean write(String log){
        byte[] logBytes = log.getBytes();
        if(currSize + logBytes.length >= maxSize){
            if(!rotate()) return false;
        }
        try {
            this.bos.write(logBytes);
            currSize += logBytes.length;
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private boolean rotate() {
        try {
            this.bos.close();
            String timestamp = String.valueOf(Instant.now().toEpochMilli());
            Path newName = this.archivedWalDirectoryPath.resolve("wal-" + timestamp + ".log");
            Files.move(activeWalPath, newName);

            bos = new BufferedOutputStream(
                    Files.newOutputStream(this.activeWalPath, StandardOpenOption.CREATE
                            , StandardOpenOption.CREATE)
            );
            currSize = 0L;
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    public void close() throws Exception {
        if(bos != null){
            bos.flush();
            bos.close();
        }
    }
}
