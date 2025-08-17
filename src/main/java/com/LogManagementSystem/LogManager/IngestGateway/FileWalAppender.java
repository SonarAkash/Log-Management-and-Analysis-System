package com.LogManagementSystem.LogManager.IngestGateway;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class FileWalAppender {

    public FileWalAppender() {
        this.nonblockingQueue = new ConcurrentLinkedQueue<>();
        this.activeWal = Paths.get("src/main/resources/WAL/activeWal.txt");
        this.archivedWal = Paths.get("src/main/resources/WAL/archivedWal.txt");
    }

    private final Path activeWal;
    private final Path archivedWal;
    Queue<String> nonblockingQueue;
    private final long fileSizeLimit = 1_000_000_000;

    public String appendLog(RawDTO log) {
        System.out.println(log);
        String uuid = UUID.randomUUID().toString();
        byte[] payload = (uuid + " : " + log.log() + "\n").getBytes();

        try (FileChannel fileChannel = FileChannel.open
                (activeWal,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND)) {

            ByteBuffer buffer = ByteBuffer.wrap(payload);
//            buffer.flip();
            while(buffer.hasRemaining()){
                int writtenBytes = fileChannel.write(buffer);
            }
            fileChannel.force(true);
//            buffer.clear();
            nonblockingQueue.offer(uuid);
            if(Files.exists(activeWal)){
                long size = Files.size(activeWal);
                if(size >= fileSizeLimit){
                    archiveWal();
                }
            }
        } catch (Exception e) {
            return null;
//            throw new RuntimeException(e);
        }
        System.out.println(nonblockingQueue);
        return uuid;
    }

    private void archiveWal(){
        try (
                BufferedReader reader = Files.newBufferedReader(activeWal);
                BufferedWriter writer = Files.newBufferedWriter(archivedWal, StandardOpenOption.APPEND)
                ) {

            String currentLine;

            while((currentLine = reader.readLine()) != null){
                writer.write(currentLine);
                writer.newLine();
            }

        } catch (Exception e) {
            System.out.println("Failed to copy WAL");
        }
    }
}
