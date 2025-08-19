package com.LogManagementSystem.LogManager.IngestGateway;

import com.LogManagementSystem.LogManager.Entity.WalProperties;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

@Service
public class FileWalAppender implements AutoCloseable{
    private long maxSize;
    private long currSize;
    private Path activeWalPath;
    private Path archivedWalDirectoryPath;
    private DataOutputStream dos;
//    private Fille
//    private BufferedOutputStream bos;

    public FileWalAppender(WalProperties pros) throws IOException {
        this.maxSize = pros.getMaxSize();
        this.activeWalPath = Paths.get(pros.getActiveWalPath());
        this.archivedWalDirectoryPath = Paths.get(pros.getArchivedWalDirectoryPath());
        if(Files.exists(activeWalPath)){
            currSize = Files.size(activeWalPath);
            dos = new DataOutputStream(new BufferedOutputStream(
                    new FileOutputStream(pros.getActiveWalPath(), true)
            ));
        }else{
            currSize = 0L;
            dos = new DataOutputStream(new BufferedOutputStream(
                    Files.newOutputStream(this.activeWalPath, StandardOpenOption.CREATE
                            , StandardOpenOption.APPEND)
            ));
        }

    }

    public synchronized boolean write(String log){
        byte[] logBytes = log.getBytes(StandardCharsets.UTF_8);
        if(currSize + logBytes.length >= maxSize){
            if(!rotate()) return false;
        }
        try {
            int size = logBytes.length;
            this.dos.writeInt(size);
            this.dos.write(logBytes);
            currSize += size;
        } catch (IOException e) {
            System.out.println("Writing Failed !!");
            return false;
        }
        return true;
    }

    private boolean rotate() {
        try {
            this.dos.close();
            String timestamp = String.valueOf(Instant.now().toEpochMilli());
            Path newName = this.archivedWalDirectoryPath.resolve("wal-" + timestamp + ".log");
            Files.move(activeWalPath, newName);
            System.out.println("file created : " + newName.toString());

//            Thread.sleep(5000);

            dos = new DataOutputStream(new BufferedOutputStream(
                    Files.newOutputStream(this.activeWalPath, StandardOpenOption.CREATE
                            , StandardOpenOption.CREATE)
            ));
            currSize = 0L;
        } catch (IOException e) {
            System.out.println("Rotating Failed !!");
            return false;
        }
        return true;
    }

    @Override
    public void close() throws Exception {
        if(dos != null){
            dos.flush();
            dos.close();
        }
    }
}
