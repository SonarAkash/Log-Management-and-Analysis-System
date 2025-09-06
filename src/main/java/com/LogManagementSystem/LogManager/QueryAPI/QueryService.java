package com.LogManagementSystem.LogManager.QueryAPI;

import com.LogManagementSystem.LogManager.Entity.LogEvent;
import com.LogManagementSystem.LogManager.LQLparser.AbstractSyntaxTree.Expr;
import com.LogManagementSystem.LogManager.LQLparser.Parser;
import com.LogManagementSystem.LogManager.LQLparser.SemanticAnalyzer;
import com.LogManagementSystem.LogManager.LQLparser.Sql.SqlGenerator;
import com.LogManagementSystem.LogManager.LQLparser.Sql.SqlQuery;
import com.LogManagementSystem.LogManager.LQLparser.Tokenizer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.LogManagementSystem.LogManager.LQLparser.Token.Token;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.UUID;


@Service
public class QueryService {


    private final ObjectProvider<Parser> provider;

    private final Tokenizer tokenizer;

    private final SemanticAnalyzer semanticAnalyzer;

    private final SqlGenerator sqlGenerator;
//    @PersistenceContext
    private final EntityManager entityManager;

    public QueryService(ObjectProvider<Parser> parser, Tokenizer tokenizer, SemanticAnalyzer semanticAnalyzer, SqlGenerator sqlGenerator, EntityManager entityManager){
        this.entityManager = entityManager;
        this.sqlGenerator = sqlGenerator;
        this.tokenizer = tokenizer;
        this.semanticAnalyzer = semanticAnalyzer;
        this.provider = parser;
    }

    public Page<LogEvent> getLogs(Pageable pageable, SearchRequestDTO searchRequestDTO, UUID tenant_id){
        String query = searchRequestDTO.q();
        List<Token> tokens = tokenizer.scanTokens(query);
        Parser parser = provider.getObject();
        parser.init(tokens);
        Expr expr = parser.parse();
        if(expr != null){
            semanticAnalyzer.analyze(expr);
            SqlQuery sqlQuery = sqlGenerator.generate(expr, tenant_id, searchRequestDTO);
            String countSql = "SELECT count(*) FROM logs WHERE " + sqlQuery.query();
           Query countQuery = entityManager.createNativeQuery(countSql);
            for(int i=0; i<sqlQuery.parameters().size(); i++){
                countQuery.setParameter(i + 1, sqlQuery.parameters().get(i));
            }
            long totalResults = ((Number) countQuery.getSingleResult()).longValue();
            String dataSql = "SELECT * FROM logs WHERE " + sqlQuery.query() + " ORDER BY ts DESC";
            Query dataQuery = entityManager.createNativeQuery(dataSql, LogEvent.class);
            for (int i = 0; i < sqlQuery.parameters().size(); i++) {
                dataQuery.setParameter(i + 1, sqlQuery.parameters().get(i));
            }
            dataQuery.setFirstResult((int) pageable.getOffset());
            dataQuery.setMaxResults(pageable.getPageSize());

            @SuppressWarnings("unchecked")
            List<LogEvent> results = dataQuery.getResultList();

            return new PageImpl<>(results, pageable, totalResults);
        }else{
            return null;
        }
    }
}
