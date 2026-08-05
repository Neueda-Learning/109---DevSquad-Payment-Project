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
import java.util.ArrayList;
import java.util.List;

@Repository
public class PaymentRepository {
    private final JdbcTemplate jdbcTemplate;
    private final PaymentRowMapper paymentRowMapper = new PaymentRowMapper();

    public PaymentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Payment createPayment(Payment request) {
        request.setInvoiceNumber("INV-" + System.currentTimeMillis());
        
        final String finalInvoiceNumber = request.getInvoiceNumber();

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
                    schedule_id,
                    payment_method_id
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?,
                        (SELECT method_type FROM PaymentMethods WHERE payment_method_id = ?),
                        ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, finalInvoiceNumber);
            preparedStatement.setLong(2, request.getSenderAccountNumber());
            preparedStatement.setLong(3, request.getReceiverAccountNumber());
            preparedStatement.setBigDecimal(4, request.getAmount());
            setNullableInt(preparedStatement, 5, request.getCurrencyId());
            preparedStatement.setDate(6, request.getPaymentDate());
            preparedStatement.setTime(7, request.getPaymentTime());
            preparedStatement.setString(8, mapModelStatus(request.getStatus()));
            preparedStatement.setString(9, request.getDescription());
            preparedStatement.setInt(10, request.getPaymentModeId());
            setNullableInt(preparedStatement, 11, request.getScheduleId());
            preparedStatement.setInt(12, request.getPaymentModeId());
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
                       schedule_id,
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
                       schedule_id,
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

    // ── Filtered list with pagination 

    public List<Payment> getPaymentsWithFilters(String status, String mode,
                                                String fromDate, String toDate,
                                                Double minAmount, Double maxAmount,
                                                int page, int size) {

        StringBuilder sql = new StringBuilder("""
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
                       schedule_id,
                       payment_method_id
                FROM Payments
                WHERE 1=1
                """);

        List<Object> params = new ArrayList<>();

        if (status != null && !status.isBlank()) {
            switch (status.toUpperCase()) {
                case "CREATED" -> sql.append(" AND status = 'CREATED'");
                case "VALIDATING" -> sql.append(" AND status = 'VALIDATED'");
                case "COMPLETED" -> sql.append(" AND status = 'COMPLETED'");
                case "FAILED" -> sql.append(" AND status = 'FAILED'");
                case "CANCELLED" -> sql.append(" AND status = 'FAILED'");
                case "IN_PROGRESS" -> sql.append(" AND status IN ('CREATED','VALIDATED')");  // All active statuses
            }
        }

        if (mode != null && !mode.isBlank()) {
            sql.append(" AND payment_mode = ?");
            params.add(mode.toUpperCase());
        }

        if (fromDate != null && !fromDate.isBlank()) {
            sql.append(" AND payment_date >= ?");
            params.add(fromDate);
        }

        if (toDate != null && !toDate.isBlank()) {
            sql.append(" AND payment_date <= ?");
            params.add(toDate);
        }

        if (minAmount != null) {
            sql.append(" AND amount >= ?");
            params.add(minAmount);
        }

        if (maxAmount != null) {
            sql.append(" AND amount <= ?");
            params.add(maxAmount);
        }

        sql.append(" ORDER BY payment_id LIMIT ? OFFSET ?");
        params.add(size);
        params.add((long) page * size);

        return jdbcTemplate.query(sql.toString(), paymentRowMapper, params.toArray());
    }

    // ── Status update

    public void updatePaymentStatus(Integer paymentId, Payment.Status targetStatus) {
        String dbStatus = mapModelStatus(targetStatus);
        String sql = "UPDATE Payments SET status = ? WHERE payment_id = ?";
        jdbcTemplate.update(sql, dbStatus, paymentId);
    }


    // ── Helpers 

    private String mapModelStatus(Payment.Status status) {
        if (status == null) {
            return "CREATED";
        }

        return switch (status) {
            case CREATED -> "CREATED";
            case VALIDATING -> "VALIDATED";
            case COMPLETED -> "COMPLETED";
            case FAILED -> "FAILED";
            case CANCELLED -> "FAILED";  // Map CANCELLED to FAILED in DB
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
