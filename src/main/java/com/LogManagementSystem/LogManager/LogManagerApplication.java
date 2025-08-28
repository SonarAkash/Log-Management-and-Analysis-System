package com.LogManagementSystem.LogManager;

import com.LogManagementSystem.LogManager.LQLparser.AbstractSyntaxTree.AstPrinter;
import com.LogManagementSystem.LogManager.LQLparser.AbstractSyntaxTree.Expr;
import com.LogManagementSystem.LogManager.LQLparser.Parser;
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
				"user.email:\"test@example.com\" and (status=500 or \"timeout\")",

				"Test Case 4: Operator Precedence",
				"NOT level:info AND service:api OR \"user logged out\"",

				"Test Case 5: Implicit ANDs with Grouping",
				"hostname:web-01 (app:nginx OR app:proxy) \"GET /index.html\"",

				"Test Case 6: Deeply Nested and Mixed Expressions",
				"A AND (B OR C (D AND NOT E))"
		);

		AstPrinter printer = new AstPrinter();

		testCases.forEach((name, query) -> {
			System.out.println("--- " + name + " ---");
			System.out.println("Query: " + query);

			try {
				// 1. Tokenize
				Tokenizer tokenizer = new Tokenizer(query);
				List<Token> tokens = tokenizer.scanTokens();

				// 2. Parse
				Parser parser = new Parser(tokens);
				Expr expression = parser.parse();

				// 3. Print AST
				if (expression != null) {
					System.out.println("Generated AST:");
					System.out.println("  " + printer.print(expression));
				} else {
					System.out.println("  Parser returned null (syntax error).");
				}
			} catch (Exception e) {
				System.out.println("  ERROR: " + e.getMessage());
			}

			System.out.println();
		});
	}

}
