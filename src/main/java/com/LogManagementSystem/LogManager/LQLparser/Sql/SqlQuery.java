package com.LogManagementSystem.LogManager.LQLparser.Sql;

import java.util.List;

/**
 * An immutable data class to hold the results of the SQL generation.
 * It contains the parameterized SQL string and the corresponding parameters.
 *
 * @param query      The generated SQL query string with '?' placeholders.
 * @param parameters The list of values to be safely passed to the PreparedStatement.
 */
public record SqlQuery(String query, List<Object> parameters) {}
