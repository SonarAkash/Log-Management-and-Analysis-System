package com.LogManagementSystem.LogManager.Config;

import com.LogManagementSystem.LogManager.Entity.LogEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Configuration
public class AppConfig {

    @Bean
    public BlockingQueue<LogEvent> injectBufferQueue(){
        return new LinkedBlockingQueue<>();
    }
}
