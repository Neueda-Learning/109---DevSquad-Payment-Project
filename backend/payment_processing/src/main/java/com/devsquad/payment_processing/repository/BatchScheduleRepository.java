package com.devsquad.payment_processing.repository;

import com.devsquad.payment_processing.model.BatchSchedule;
import com.devsquad.payment_processing.model.BatchScheduleRecipient;
import com.devsquad.payment_processing.model.BatchScheduleStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class BatchScheduleRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<BatchSchedule> batchScheduleRowMapper = (rs, rowNum) -> new BatchSchedule(
            rs.getLong("batch_schedule_id"),
            rs.getString("batch_id"),
            rs.getLong("sender_account_number"),
            rs.getInt("payment_method_id"),
            rs.getString("description"),
            rs.getDate("scheduled_date"),
            BatchScheduleStatus.valueOf(rs.getString("status"))
    );

    public BatchScheduleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long createBatchSchedule(BatchSchedule schedule) {
        String sql = """
                INSERT INTO BatchSchedules (
                    batch_id,
                    sender_account_number,
                    payment_method_id,
                    description,
                    scheduled_date,
                    status
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, schedule.getBatchId());
            ps.setLong(2, schedule.getSenderAccountNumber());
            ps.setInt(3, schedule.getPaymentModeId());
            setNullableString(ps, 4, schedule.getDescription());
            ps.setDate(5, schedule.getScheduledDate());
            ps.setString(6, schedule.getStatus().name());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key == null ? null : key.longValue();
    }

    public void addRecipients(Long batchScheduleId, List<BatchScheduleRecipient> recipients) {
        String sql = """
                INSERT INTO BatchScheduleRecipients (
                    batch_schedule_id,
                    receiver_account_number,
                    amount,
                    currency_id,
                    description
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        for (BatchScheduleRecipient recipient : recipients) {
            jdbcTemplate.update(sql,
                    batchScheduleId,
                    recipient.getReceiverAccountNumber(),
                    recipient.getAmount(),
                    recipient.getCurrencyId(),
                    recipient.getDescription());
        }
    }

    public List<BatchSchedule> getDueBatchSchedules() {
        String sql = """
                SELECT batch_schedule_id,
                       batch_id,
                       sender_account_number,
                       payment_method_id,
                       description,
                       scheduled_date,
                       status
                FROM BatchSchedules
                WHERE status = 'SCHEDULED'
                  AND scheduled_date <= CURDATE()
                ORDER BY batch_schedule_id
                """;

        return jdbcTemplate.query(sql, batchScheduleRowMapper);
    }

    public List<Map<String, Object>> getAllBatchScheduleSummaries() {
        String sql = """
                SELECT bs.batch_schedule_id,
                       bs.batch_id,
                       bs.scheduled_date,
                       bs.created_at,
                       bs.status,
                       COUNT(bsr.batch_schedule_recipient_id) AS total_recipients,
                       COALESCE(SUM(bsr.amount), 0) AS total_amount
                FROM BatchSchedules bs
                LEFT JOIN BatchScheduleRecipients bsr
                  ON bsr.batch_schedule_id = bs.batch_schedule_id
                GROUP BY bs.batch_schedule_id, bs.batch_id, bs.scheduled_date, bs.created_at, bs.status
                ORDER BY bs.batch_schedule_id DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("batchScheduleId", rs.getLong("batch_schedule_id"));
            summary.put("batchId", rs.getString("batch_id"));
            summary.put("scheduledDate", rs.getDate("scheduled_date"));
            summary.put("createdAt", rs.getTimestamp("created_at"));
            summary.put("status", rs.getString("status"));
            summary.put("totalRecipients", rs.getInt("total_recipients"));
            summary.put("totalAmount", rs.getBigDecimal("total_amount"));
            return summary;
        });
    }

    public Map<String, Object> getBatchScheduleDetailsByBatchId(String batchId) {
        String sql = """
                SELECT bs.batch_schedule_id,
                       bs.batch_id,
                       bs.scheduled_date,
                       bs.created_at,
                       bs.executed_at,
                       bs.status,
                       bs.description,
                       COUNT(bsr.batch_schedule_recipient_id) AS total_recipients,
                       COALESCE(SUM(bsr.amount), 0) AS total_amount
                FROM BatchSchedules bs
                LEFT JOIN BatchScheduleRecipients bsr
                  ON bsr.batch_schedule_id = bs.batch_schedule_id
                WHERE bs.batch_id = ?
                GROUP BY bs.batch_schedule_id, bs.batch_id, bs.scheduled_date, bs.created_at,
                         bs.executed_at, bs.status, bs.description
                """;

        List<Map<String, Object>> rows = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> details = new LinkedHashMap<>();
            details.put("batchScheduleId", rs.getLong("batch_schedule_id"));
            details.put("batchId", rs.getString("batch_id"));
            details.put("scheduledDate", rs.getDate("scheduled_date"));
            details.put("createdAt", rs.getTimestamp("created_at"));
            details.put("executedAt", rs.getTimestamp("executed_at"));
            details.put("status", rs.getString("status"));
            details.put("description", rs.getString("description"));
            details.put("totalRecipients", rs.getInt("total_recipients"));
            details.put("totalAmount", rs.getBigDecimal("total_amount"));
            return details;
        }, batchId);

        if (rows.isEmpty()) {
            return null;
        }

        Map<String, Object> details = rows.get(0);
        details.put("payments", getBatchScheduleRecipientsByBatchId(batchId));
        return details;
    }

    private List<Map<String, Object>> getBatchScheduleRecipientsByBatchId(String batchId) {
        String sql = """
                SELECT bsr.receiver_account_number,
                       bsr.amount,
                       bsr.currency_id,
                       bsr.description
                FROM BatchScheduleRecipients bsr
                INNER JOIN BatchSchedules bs
                  ON bs.batch_schedule_id = bsr.batch_schedule_id
                WHERE bs.batch_id = ?
                ORDER BY bsr.batch_schedule_recipient_id
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> recipient = new LinkedHashMap<>();
            recipient.put("receiverAccountNumber", rs.getLong("receiver_account_number"));
            recipient.put("amount", rs.getBigDecimal("amount"));
            recipient.put("currencyId", rs.getObject("currency_id", Integer.class));
            recipient.put("description", rs.getString("description"));
            return recipient;
        }, batchId);
    }

    public List<BatchScheduleRecipient> getRecipients(Long batchScheduleId) {
        String sql = """
                SELECT receiver_account_number,
                       amount,
                       currency_id,
                       description
                FROM BatchScheduleRecipients
                WHERE batch_schedule_id = ?
                ORDER BY batch_schedule_recipient_id
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new BatchScheduleRecipient(
                rs.getLong("receiver_account_number"),
                rs.getBigDecimal("amount"),
                rs.getObject("currency_id", Integer.class),
                rs.getString("description")
        ), batchScheduleId);
    }

    public void markProcessing(Long batchScheduleId) {
        updateStatus(batchScheduleId, BatchScheduleStatus.PROCESSING, null);
    }

    public void markCompleted(Long batchScheduleId, BatchScheduleStatus status) {
        String sql = """
                UPDATE BatchSchedules
                SET status = ?, executed_at = NOW(), last_error = NULL
                WHERE batch_schedule_id = ?
                """;
        jdbcTemplate.update(sql, status.name(), batchScheduleId);
    }

    public void markFailed(Long batchScheduleId, String errorMessage) {
        updateStatus(batchScheduleId, BatchScheduleStatus.FAILED, errorMessage);
    }

    private void updateStatus(Long batchScheduleId, BatchScheduleStatus status, String errorMessage) {
        String sql = """
                UPDATE BatchSchedules
                SET status = ?, last_error = ?
                WHERE batch_schedule_id = ?
                """;

        jdbcTemplate.update(sql, status.name(), errorMessage, batchScheduleId);
    }

    private void setNullableString(PreparedStatement ps, int index, String value) throws java.sql.SQLException {
        if (value != null && !value.isBlank()) {
            ps.setString(index, value);
        } else {
            ps.setNull(index, Types.VARCHAR);
        }
    }
}

