package com.fidelity.moneytransfer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@Configuration
public class SnowflakeConfig {

    @Value("${snowflake.url}")
    private String url;

    @Value("${snowflake.user}")
    private String user;

    @Value("${snowflake.password}")
    private String password;

    @Value("${snowflake.warehouse}")
    private String warehouse;

    @Value("${snowflake.database}")
    private String database;

    @Value("${snowflake.schema}")
    private String schema;

    @Value("${snowflake.role}")
    private String role;

    /**
     * Create a Snowflake JDBC Connection
     */
    @Bean
    public Connection snowflakeConnection() throws SQLException {
        try {
            Class.forName("net.snowflake.client.jdbc.SnowflakeDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Snowflake JDBC Driver not found", e);
        }

        Properties props = new Properties();
        props.put("user", user);
        props.put("password", password);
        props.put("database", database);
        props.put("schema", schema);
        props.put("warehouse", warehouse);
        props.put("role", role);

        return DriverManager.getConnection(url, props);
    }

    /**
     * Wrap the Snowflake connection in a JdbcTemplate for easier Spring use
     */
    @Bean
    public JdbcTemplate snowflakeJdbcTemplate(Connection snowflakeConnection) {
        // Wrap in SingleConnectionDataSource to use JdbcTemplate
        SingleConnectionDataSource ds = new SingleConnectionDataSource(snowflakeConnection, true);
        return new JdbcTemplate(ds);
    }
}
