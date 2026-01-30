package com.example.platform.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
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
     * Execute a stored function that returns a single row, mapped using a RowMapper
     * PostgreSQL functions that return TABLE(...) need this approach
     * 
     * @param functionName Name of the stored function
     * @param params Named parameters map (must be LinkedHashMap to preserve order)
     * @param rowMapper Row mapper to convert ResultSet to object
     * @return Mapped result object or null if no rows returned
     */
    @FunctionalInterface
    public interface RowMapperFunction<T> {
        T map(ResultSet rs) throws SQLException;
    }
    
    protected <T> T executeQuery(String functionName, Map<String, Object> params, RowMapperFunction<T> mapper) {
        // Build SQL call for PostgreSQL function
        StringBuilder sql = new StringBuilder("SELECT * FROM ").append(functionName).append("(");
        
        // Add named parameter placeholders for JDBC
        boolean first = true;
        for (String key : params.keySet()) {
            if (!first) sql.append(", ");
            // Use named parameters in proper order
            sql.append("?");
            first = false;
        }
        sql.append(")");
        
        // Prepare parameter values in same order as keys
        Object[] paramValues = params.values().toArray();
        
        try {
            return jdbcTemplate.queryForObject(sql.toString(), (rs, rowNum) -> mapper.map(rs), paramValues);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
    
    /**
     * Get the underlying JdbcTemplate for custom queries
     * Use sparingly - prefer stored procedures
     */
    protected JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }
}
