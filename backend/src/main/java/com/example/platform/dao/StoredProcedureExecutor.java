package com.example.platform.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Base class for calling stored procedures
 * SP-first approach: All database access must go through stored procedures
 * No SQL string concatenation allowed
 */
@Component
public class StoredProcedureExecutor {
    
    private final JdbcTemplate jdbcTemplate;
    
    public StoredProcedureExecutor(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    /**
     * Execute a stored procedure with named parameters
     * 
     * @param procedureName Name of the stored procedure
     * @param params Named parameters map
     * @return Result map from the stored procedure
     */
    protected Map<String, Object> execute(String procedureName, Map<String, Object> params) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
            .withProcedureName(procedureName);
        
        return jdbcCall.execute(params);
    }
    
    /**
     * Execute a stored function with named parameters
     * 
     * @param functionName Name of the stored function
     * @param params Named parameters map
     * @return Result from the stored function
     */
    protected <T> T executeFunction(String functionName, Map<String, Object> params, Class<T> returnType) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
            .withFunctionName(functionName);
        
        return jdbcCall.executeFunction(returnType, params);
    }
    
    /**
     * Get the underlying JdbcTemplate for custom queries
     * Use sparingly - prefer stored procedures
     */
    protected JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }
}
