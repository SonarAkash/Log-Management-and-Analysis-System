package com.LogManagementSystem.LogManager;

import com.LogManagementSystem.LogManager.LQLparser.AbstractSyntaxTree.AstPrinter;
import com.LogManagementSystem.LogManager.LQLparser.AbstractSyntaxTree.Expr;
import com.LogManagementSystem.LogManager.LQLparser.Parser;
import com.LogManagementSystem.LogManager.LQLparser.SemanticAnalyzer;
import com.LogManagementSystem.LogManager.LQLparser.Sql.SqlGenerator;
import com.LogManagementSystem.LogManager.LQLparser.Sql.SqlQuery;
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
//		Map<String, String> testCases = Map.of(
//				"Test Case 1: Complex Key-Value and Implicit AND",
//				"service:api-gateway client_ip=\"192.168.1.1\" response_time_ms=250",
//
//				"Test Case 2: Nested Groups and Full-Text Search",
//				"(level:error OR (app.name:auth AND \"login failed\")) AND NOT _exists_:trace.id",
//
//				"Test Case 3: Edge Cases and Mixed Syntax",
//				"user.email:\"test@example.com\" and (status=500 or \"timeout\")",
//
//				"Test Case 4: Operator Precedence",
//				"NOT level:info AND service:api OR \"user logged out\"",
//
//				"Test Case 5: Implicit ANDs with Grouping",
//				"hostname:web-01 (app:nginx OR app:proxy) \"GET /index.html\"",
//
//				"Test Case 6: Deeply Nested and Mixed Expressions",
//				"A AND (B OR C (D AND NOT E))"
//
////				"Test Case 7: Invalid Semantic Logic",
////				"level:error AND status:500 AND level:warning"
//		);
//
//
//		testCases.forEach((name, query) -> {
//			System.out.println("--- " + name + " ---");
//			System.out.println("Query: " + query);
//
//			try {
//				// 1. Tokenize
//				Tokenizer tokenizer = new Tokenizer();
//				List<Token> tokens = tokenizer.scanTokens(query);
//
//				// 2. Parse
//				Parser parser = new Parser();
//				parser.init(tokens);
//				Expr expression = parser.parse();
//				if (expression != null) {
//					// 3. Analyze (NEW STEP)
//					SemanticAnalyzer analyzer = new SemanticAnalyzer();
//					analyzer.analyze(expression);
//					System.out.println("  ✅ Semantic analysis passed.");
//
////					// 4. Print AST
////					AstPrinter printer = new AstPrinter();
////					System.out.println("  Generated AST:");
////					System.out.println("    " + printer.print(expression));
//
//					// 4. Generate SQL (NEW STEP)
//					SqlGenerator generator = new SqlGenerator();
//					SqlQuery sqlQuery = generator.generate(expression);
//					System.out.println("\n--- FINAL OUTPUT ---");
//					System.out.println("Generated SQL WHERE clause:");
//					System.out.println("  " + sqlQuery.query());
//					System.out.println("\nParameters:");
//					System.out.println("  " + sqlQuery.parameters());
//				} else {
//					System.out.println("  ❌ Parser returned null (syntax error).");
//				}
//			} catch (Exception e) {
//				// Now this can catch ParseError or SemanticException
//				System.out.println("  ❌ ERROR: " + e.getMessage());
//				e.printStackTrace(); // Helpful for debugging
//			}
//
//			System.out.println();
//		});
	}

}
