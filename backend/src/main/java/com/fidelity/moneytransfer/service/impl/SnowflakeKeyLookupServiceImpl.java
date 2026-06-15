package com.fidelity.moneytransfer.service.impl;

import com.fidelity.moneytransfer.service.SnowflakeKeyLookupService;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

@Service
public class SnowflakeKeyLookupServiceImpl implements SnowflakeKeyLookupService {

    private final Connection snowflakeConnection;

    public SnowflakeKeyLookupServiceImpl(Connection snowflakeConnection) {
        this.snowflakeConnection = snowflakeConnection;
    }

    @Override
    public long getAccountKey(long accountId) {

        String sql = """
            SELECT account_key
            FROM DIM_ACCOUNT
            WHERE account_id = ?
        """;

        try (PreparedStatement ps = snowflakeConnection.prepareStatement(sql)) {
            ps.setLong(1, accountId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getLong("account_key");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        throw new RuntimeException("DIM_ACCOUNT key not found for account_id=" + accountId);
    }

    @Override
    public int getDateKey(LocalDate date) {

        String sql = """
            SELECT date_key
            FROM DIM_DATE
            WHERE full_date = ?
        """;

        try (PreparedStatement ps = snowflakeConnection.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(date));
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("date_key");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        throw new RuntimeException("DIM_DATE key not found for date=" + date);
    }
}
