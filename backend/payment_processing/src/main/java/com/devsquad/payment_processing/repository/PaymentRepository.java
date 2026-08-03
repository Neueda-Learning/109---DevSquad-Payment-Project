package com.devsquad.payment_processing.repository;

import com.devsquad.payment_processing.model.Payment;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

@Repository
public class PaymentRepository {
    private final JdbcTemplate jdbcTemplate;
    private final PaymentRowMapper paymentRowMapper = new PaymentRowMapper();

    public PaymentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Payment createPayment(Payment request) {
        String sql = """
                INSERT INTO Payments (
                    payment_invoice_number,
                    sender_account_number,
                    receiver_account_number,
                    amount,
                    currency_id,
                    payment_date,
                    payment_time,
                    status,
                    description,
                    payment_mode,
                    is_scheduled_payment,
                    schedule_period,
                    payment_method_id
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?,
                        (SELECT method_type FROM PaymentMethods WHERE payment_method_id = ?),
                        ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, request.getInvoiceNumber());
            preparedStatement.setLong(2, request.getSenderAccountNumber());
            preparedStatement.setLong(3, request.getReceiverAccountNumber());
            preparedStatement.setDouble(4, request.getAmount());
            setNullableInt(preparedStatement, 5, request.getCurrencyId());
            preparedStatement.setDate(6, request.getPaymentDate());
            preparedStatement.setTime(7, request.getPaymentTime());
            preparedStatement.setString(8, mapModelStatus(request.getStatus()));
            preparedStatement.setString(9, request.getDescription());
            preparedStatement.setInt(10, request.getPaymentModeId());
            preparedStatement.setBoolean(11, request.isScheduled());
            preparedStatement.setString(12, request.getSchedulePeriod());
            preparedStatement.setInt(13, request.getPaymentModeId());
            return preparedStatement;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();
        if (generatedId != null) {
            request.setPaymentId(generatedId.intValue());
        }

        return request;
    }

    public Payment getPaymentById(Integer paymentId) {
        String sql = """
                SELECT payment_id,
                       payment_invoice_number,
                       sender_account_number,
                       receiver_account_number,
                       amount,
                       currency_id,
                       payment_date,
                       payment_time,
                       status,
                       description,
                       is_scheduled_payment,
                       schedule_period,
                       payment_method_id
                FROM Payments
                WHERE payment_id = ?
                """;

        try {
            return jdbcTemplate.queryForObject(sql, paymentRowMapper, paymentId);
        } catch (EmptyResultDataAccessException exception) {
            return null;
        }
    }

    public List<Payment> getAllPayments() {
        String sql = """
                SELECT payment_id,
                       payment_invoice_number,
                       sender_account_number,
                       receiver_account_number,
                       amount,
                       currency_id,
                       payment_date,
                       payment_time,
                       status,
                       description,
                       is_scheduled_payment,
                       schedule_period,
                       payment_method_id
                FROM Payments
                ORDER BY payment_id
                """;

        return jdbcTemplate.query(sql, paymentRowMapper);
    }

    public void deletePayment(Integer paymentId) {
        String sql = """
                DELETE FROM Payments
                WHERE payment_id = ?
                """;

        jdbcTemplate.update(sql, paymentId);
    }


    private String mapModelStatus(Payment.Status status) {
        if (status == null) {
            return "CREATED";
        }

        return switch (status) {
            case PENDING -> "CREATED";
            case COMPLETED -> "COMPLETED";
            case FAILED, CANCELLED -> "FAILED";
        };
    }

    private void setNullableInt(PreparedStatement preparedStatement, int index, Integer value) throws SQLException {
        if (value != null) {
            preparedStatement.setInt(index, value);
        } else {
            preparedStatement.setNull(index, Types.INTEGER);
        }
    }
}
