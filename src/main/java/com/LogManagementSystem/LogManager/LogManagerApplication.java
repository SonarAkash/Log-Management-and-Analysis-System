package com.LogManagementSystem.LogManager;

import com.LogManagementSystem.LogManager.LQLparser.Token.Token;
import com.LogManagementSystem.LogManager.LQLparser.Tokenizer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;
import java.util.Map;

@SpringBootApplication
public class LogManagerApplication {

	public static void main(String[] args) {

		SpringApplication.run(LogManagerApplication.class, args);
		Map<String, String> testCases = Map.of(
				"Test Case 1: Complex Key-Value and Implicit AND",
				"service:api-gateway client_ip=\"192.168.1.1\" response_time_ms=250",

				"Test Case 2: Nested Groups and Full-Text Search",
				"(level:error OR (app.name:auth AND \"login failed\")) AND NOT _exists_:trace.id",

				"Test Case 3: Edge Cases and Mixed Syntax",
				"user.email:\"test@example.com\" and (status=500 or \"timeout\")"
		);

		testCases.forEach((name, query) -> {
			System.out.println("--- " + name + " ---");
			System.out.println("Query: " + query);
			System.out.println("Tokens:");

			try {
				Tokenizer tokenizer = new Tokenizer(query);
				List<Token> tokens = tokenizer.scanTokens();
				for (Token token : tokens) {
					System.out.println("  " + token);
				}
			} catch (Exception e) {
				System.out.println("  ERROR: " + e.getMessage());
			}

			System.out.println(); // Add a blank line for readability
		});
	}

}
