package com.LogManagementSystem.LogManager.LQLparser.Sql;

import com.LogManagementSystem.LogManager.LQLparser.AbstractSyntaxTree.*;
import com.LogManagementSystem.LogManager.LQLparser.Token.Token;
import com.LogManagementSystem.LogManager.LQLparser.Token.TokenType;
import com.LogManagementSystem.LogManager.QueryAPI.SearchRequestDTO;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SqlGenerator implements Expr.Visitor<SqlQuery>{


    // A set of known, top-level columns in the 'logs' table.
    // This helps to decide whether to query a column directly or the 'attrs' JSONB field.
    private static final Set<String> KNOWN_COLUMNS = Set.of(
            "ts", "message", "service", "level", "hostname", "client_ip"
    );

    public SqlQuery generate(Expr expression, UUID tenantId, SearchRequestDTO searchRequestDTO) {
        if (expression == null) {
            return new SqlQuery("", new ArrayList<>());
        }
        OffsetDateTime start = null;
        if (searchRequestDTO.start() != null && !searchRequestDTO.start().isEmpty()) {
            start = OffsetDateTime.parse(searchRequestDTO.start());
        }

        OffsetDateTime end = null;
        if (searchRequestDTO.end() != null && !searchRequestDTO.end().isEmpty()) {
            end = OffsetDateTime.parse(searchRequestDTO.end());
        }
        List<Object> finalParameters = new ArrayList<>();
        SqlQuery lqlQuery = expression.accept(this);
        String q = "";
        finalParameters.add(tenantId);
        if (start != null && end != null) {
            q = "ts BETWEEN ? AND ?) AND (";
            finalParameters.add(start);
            finalParameters.add(end);
        }
        String finalSql = "(tenant_id = ?) AND (" + q + lqlQuery.query() + ")";
        finalParameters.addAll(lqlQuery.parameters());
//        System.out.println(finalSql);
//        System.out.println(finalParameters);

        return new SqlQuery(finalSql, finalParameters);
    }

    @Override
    public SqlQuery visitBinaryExpr(Binary expr) {
        // Recursively generate SQL for the left and right sides.
        SqlQuery leftQuery = expr.left().accept(this);
        SqlQuery rightQuery = expr.right().accept(this);

        // Combine the results.
        String operator = expr.operator().lexeme().toUpperCase();
        if (operator.contains("IMPLICIT")) {
            operator = "AND"; // Treat implicit ANDs as regular ANDs.
        }

        String sql = String.format("(%s %s %s)", leftQuery.query(), operator, rightQuery.query());

        List<Object> parameters = Stream.concat(
                leftQuery.parameters().stream(),
                rightQuery.parameters().stream()
        ).collect(Collectors.toList());

        return new SqlQuery(sql, parameters);
    }

    @Override
    public SqlQuery visitGroupingExpr(Grouping expr) {
        // Recursively generate SQL for the inner expression.
        SqlQuery innerQuery = expr.expression().accept(this);
        // Wrap the result in parentheses.
        String sql = String.format("(%s)", innerQuery.query());
        return new SqlQuery(sql, innerQuery.parameters());
    }

    @Override
    public SqlQuery visitLiteralExpr(Literal expr) {
        Token token = expr.value();
        if (token.type() == TokenType.KEY_VALUE) {
            return generateKeyValueQuery(token);
        } else if (token.type() == TokenType.BAREWORD) {
            return generateBarewordQuery(token);
        }
        // Should not happen in a valid AST.
        return new SqlQuery("", new ArrayList<>());
    }

    @Override
    public SqlQuery visitUnaryExpr(Unary expr) {
        // Recursively generate SQL for the operand.
        SqlQuery innerQuery = expr.right().accept(this);
        // Prepend the NOT operator.
        String sql = String.format("NOT (%s)", innerQuery.query());
        return new SqlQuery(sql, innerQuery.parameters());
    }


    private SqlQuery generateKeyValueQuery(Token token) {
        String key = extractKeyFromToken(token);
        String value = extractValueFromToken(token);

        if (key == null || value == null) {
            return new SqlQuery("1 = 1", new ArrayList<>()); // Or handle as an error
            // This should not happen, if it had been the case the error would
            // have been caught in the early phases.
        }

        // Check for the special '_exists_' keyword.
        if ("_exists_".equals(key)) {
            // The "value" from the token is the actual key I want to check for existence.
            String sql = "attrs ? ?";
            List<Object> params = List.of(value);
            return new SqlQuery(sql, params);
        }

        if (KNOWN_COLUMNS.contains(key)) {
            // It's a direct column query.
            String sql = String.format("%s = ?", key);
            List<Object> params = List.of(value);
            return new SqlQuery(sql, params);
        } else {
            // It's a query against the JSONB 'attrs' column.
            // Here, both the key and the value are parameters.
            String sql = "attrs ->> ? = ?";
            List<Object> params = List.of(key, value);
            return new SqlQuery(sql, params);
        }
    }

    private SqlQuery generateBarewordQuery(Token token) {
        String value = token.lexeme();
        // Strip quotes for FTS
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }

        // Prepare the value for to_tsquery by replacing spaces with '&'
        String ftsQueryValue = value.trim().replace(" ", " & ");

        // Query against both the message and the attrs FTS indexes.
        String sql = "(message_tsv @@ to_tsquery('english', ?) OR jsonb_to_tsvector('english', attrs, '[\"string\"]') @@ to_tsquery('english', ?))";
        List<Object> params = List.of(ftsQueryValue, ftsQueryValue);

        return new SqlQuery(sql, params);
    }

    private String extractKeyFromToken(Token token) {
        String lexeme = token.lexeme();
        int separatorIndex = lexeme.indexOf(':');
        if (separatorIndex == -1) separatorIndex = lexeme.indexOf('=');
        if (separatorIndex != -1) return lexeme.substring(0, separatorIndex).trim();
        return null;
    }

    private String extractValueFromToken(Token token) {
        String lexeme = token.lexeme();
        int separatorIndex = lexeme.indexOf(':');
        if (separatorIndex == -1) separatorIndex = lexeme.indexOf('=');
        if (separatorIndex != -1 && separatorIndex < lexeme.length() - 1) {
            String value = lexeme.substring(separatorIndex + 1).trim();
            if (value.startsWith("\"") && value.endsWith("\"")) {
                return value.substring(1, value.length() - 1);
            }
            return value;
        }
        return null;
    }
}
