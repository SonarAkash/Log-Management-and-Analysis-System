package com.LogManagementSystem.LogManager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

//import com.giffing.bucket4j.spring.boot.starter.config.filter.Bucket4JBaseConfiguration;

@SpringBootApplication
//@Import(Bucket4JBaseConfiguration.class)
public class LogManagerApplication {

	public static void main(String[] args) {

		SpringApplication.run(LogManagerApplication.class, args);
	}

}
