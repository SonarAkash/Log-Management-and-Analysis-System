package com.LogManagementSystem.LogManager.LQLparser;


import com.LogManagementSystem.LogManager.LQLparser.Token.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Tokenizer (or Lexer) is responsible for scanning the raw LQL query string
 * and converting it into a sequence of tokens.
 */
public class Tokenizer {

    private final String query;
    private final List<Token> tokens = new ArrayList<>();

    private static final Map<String, TokenType> KEYWORDS = Map.of(
            "and", TokenType.AND,
            "or", TokenType.OR,
            "not", TokenType.NOT
    );

    // Pattern to split the query by spaces, while keeping quoted strings and parentheses intact.
    private static final Pattern TOKEN_SPLITTER = Pattern.compile("\"[^\"]*\"|\\(|\\)|[^\\s()]+");

    // Simple, targeted patterns to CLASSIFY the words found by the splitter.
    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("^\\w+(\\.\\w+|\\[\\w+\\])*[:=].+$");
    private static final Pattern LPAREN_PATTERN = Pattern.compile("^\\($");
    private static final Pattern RPAREN_PATTERN = Pattern.compile("^\\)$");


    public Tokenizer(String query) {
        this.query = query;
    }

    public List<Token> scanTokens() {
        Matcher matcher = TOKEN_SPLITTER.matcher(query);

        while (matcher.find()) {
            String lexeme = matcher.group(0);
            int position = matcher.start();

            TokenType type;

            if (KEYWORDS.containsKey(lexeme.toLowerCase())) {
                type = KEYWORDS.get(lexeme.toLowerCase());
            } else if (LPAREN_PATTERN.matcher(lexeme).matches()) {
                type = TokenType.LPAREN;
            } else if (RPAREN_PATTERN.matcher(lexeme).matches()) {
                type = TokenType.RPAREN;
            } else if (KEY_VALUE_PATTERN.matcher(lexeme).matches()) {
                type = TokenType.KEY_VALUE;
            } else {
                type = TokenType.BAREWORD;
            }
            tokens.add(new Token(type, lexeme, position));
        }

        tokens.add(new Token(TokenType.EOF, "", query.length()));
        return tokens;
    }
}
