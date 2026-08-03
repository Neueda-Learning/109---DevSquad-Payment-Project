package com.devsquad.payment_processing.repository;

import com.devsquad.payment_processing.model.Payment;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PaymentRowMapper implements RowMapper<Payment> {

    @Override
    public Payment mapRow(ResultSet rs, int rowNum) throws SQLException {
        Long rawId = rs.getObject("payment_id", Long.class);
        return new Payment(
                rawId != null ? rawId.intValue() : null,
                rs.getString("payment_invoice_number"),
                rs.getLong("sender_account_number"),
                rs.getLong("receiver_account_number"),
                rs.getDouble("amount"),
                rs.getObject("currency_id", Integer.class),
                rs.getInt("payment_method_id"),
                rs.getDate("payment_date"),
                rs.getTime("payment_time"),
                rs.getString("description"),
                rs.getBoolean("is_scheduled_payment"),
                rs.getString("schedule_period"),
                mapDatabaseStatus(rs.getString("status"))
        );
    }

    private Payment.Status mapDatabaseStatus(String databaseStatus) {
        return switch (databaseStatus) {
            case "COMPLETED" -> Payment.Status.COMPLETED;
            case "FAILED" -> Payment.Status.FAILED;
            case "CREATED", "VALIDATED", "SENT" -> Payment.Status.PENDING;
            default -> throw new IllegalArgumentException("Unsupported payment status: " + databaseStatus);
        };
    }
}
