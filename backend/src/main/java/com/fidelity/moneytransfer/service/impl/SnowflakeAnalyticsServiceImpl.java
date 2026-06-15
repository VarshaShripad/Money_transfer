package com.fidelity.moneytransfer.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.fidelity.moneytransfer.service.SnowflakeAnalyticsService;
import java.sql.Timestamp;

@Service
public class SnowflakeAnalyticsServiceImpl implements SnowflakeAnalyticsService {

    private final JdbcTemplate snowflakeJdbcTemplate;

    public SnowflakeAnalyticsServiceImpl(JdbcTemplate snowflakeJdbcTemplate) {
        this.snowflakeJdbcTemplate = snowflakeJdbcTemplate;
        System.out.println("[SnowflakeAnalyticsServiceImpl] Bean created with JdbcTemplate: " + snowflakeJdbcTemplate);
    }

    @Override
    public void recordTransaction(long fromAccountId, long toAccountId, BigDecimal amount, String status) {
        System.out.println("[recordTransaction] Called with values -> fromAccountId: " + fromAccountId
                + ", toAccountId: " + toAccountId + ", amount: " + amount + ", status: " + status);

        // Use ZonedDateTime to get the timestamp in the desired time zone (e.g., Asia/Kolkata)
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        Timestamp timestamp = Timestamp.from(zonedDateTime.toInstant());

        String sql = """
                INSERT INTO ANALYTICS.TX_RAW
                (from_key, to_key, amount, status, created_ts)
                VALUES (?, ?, ?, ?, ?)
                """;

        try {
            int rows = snowflakeJdbcTemplate.update(sql, fromAccountId, toAccountId, amount, status, timestamp);
            System.out.println("[recordTransaction] Rows inserted into TX_RAW: " + rows);
        } catch (Exception e) {
            // Log error but do not fail main transaction
            System.err.println("[recordTransaction] Snowflake TX_RAW insert failed — main transfer continues");
            e.printStackTrace();
        }
    }
}
