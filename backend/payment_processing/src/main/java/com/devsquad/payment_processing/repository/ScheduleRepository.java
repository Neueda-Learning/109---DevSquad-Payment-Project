package com.devsquad.payment_processing.repository;

import com.devsquad.payment_processing.model.Frequency;
import com.devsquad.payment_processing.model.Schedule;
import com.devsquad.payment_processing.model.ScheduleStatus;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ScheduleRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ScheduleRowMapper scheduleRowMapper = new ScheduleRowMapper();

    public ScheduleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Create Schedule
    public Schedule createSchedule(Schedule schedule) {
        String sql = """
                INSERT INTO Schedules (
                    sender_account_number,
                    receiver_account_number,
                    amount,
                    currency_id,
                    payment_method_id,
                    description,
                    frequency,
                    start_date,
                    end_date,
                    next_run_date,
                    last_run_date,
                    status
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, schedule.getSenderAccountNumber());
            ps.setLong(2, schedule.getReceiverAccountNumber());
            ps.setBigDecimal(3, schedule.getAmount());
            setNullableInt(ps, 4, schedule.getCurrencyId());
            ps.setInt(5, schedule.getPaymentModeId());
            ps.setString(6, schedule.getDescription());
            ps.setString(7, schedule.getFrequency() != null ? schedule.getFrequency().name() : null);
            ps.setDate(8, schedule.getStartDate());
            ps.setDate(9, schedule.getEndDate());
            // default next_run_date to start_date if not provided
            ps.setDate(10, schedule.getNextRunDate() != null
                    ? schedule.getNextRunDate()
                    : schedule.getStartDate());
            ps.setDate(11, schedule.getLastRunDate());
            ps.setString(12, schedule.getStatus() != null
                    ? schedule.getStatus().name()
                    : ScheduleStatus.ACTIVE.name());
            return ps;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();
        if (generatedId != null) {
            schedule.setScheduleId(generatedId.intValue());
        }

        return schedule;
    }

    // Get Schedule by ID
    public Schedule getScheduleById(Integer scheduleId) {
        String sql = """
                SELECT schedule_id,
                       sender_account_number,
                       receiver_account_number,
                       amount,
                       currency_id,
                       payment_method_id,
                       description,
                       frequency,
                       start_date,
                       end_date,
                       next_run_date,
                       last_run_date,
                       status
                FROM Schedules
                WHERE schedule_id = ?
                """;

        try {
            return jdbcTemplate.queryForObject(sql, scheduleRowMapper, scheduleId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // Update Schedule
    public Schedule updateSchedule(Long scheduleId, Schedule schedule) {
        String sql = """
                UPDATE Schedules
                SET amount            = ?,
                    currency_id       = ?,
                    payment_method_id = ?,
                    description       = ?,
                    frequency         = ?,
                    start_date        = ?,
                    end_date          = ?,
                    next_run_date     = ?,
                    status            = ?
                WHERE schedule_id = ?
                """;

        jdbcTemplate.update(sql,
                schedule.getAmount(),
                schedule.getCurrencyId(),
                schedule.getPaymentModeId(),
                schedule.getDescription(),
                schedule.getFrequency() != null ? schedule.getFrequency().name() : null,
                schedule.getStartDate(),
                schedule.getEndDate(),
                schedule.getNextRunDate(),
                schedule.getStatus() != null ? schedule.getStatus().name() : null,
                scheduleId);

        schedule.setScheduleId(scheduleId.intValue());
        return schedule;
    }

    // Delete Schedule
    public void deleteSchedule(Integer scheduleId) {
        String sql = """
                DELETE FROM Schedules
                WHERE schedule_id = ?
                """;

        jdbcTemplate.update(sql, scheduleId);
    }
//
//    // Get execution details for a schedule
//    public Map<String, Object> getScheduleExecution(Long scheduleId) {
//        Schedule schedule = getScheduleById(scheduleId.intValue());
//        if (schedule == null) {
//            return null;
//        }
//
//        Map<String, Object> execution = new HashMap<>();
//        execution.put("scheduleId", schedule.getScheduleId());
//        execution.put("status", schedule.getStatus());
//        execution.put("frequency", schedule.getFrequency());
//        execution.put("startDate", schedule.getStartDate());
//        execution.put("endDate", schedule.getEndDate());
//        execution.put("lastRunDate", schedule.getLastRunDate());
//        execution.put("nextRunDate", schedule.getNextRunDate());
//        return execution;
//    }


    private void setNullableInt(PreparedStatement ps, int index, Integer value)
            throws java.sql.SQLException {
        if (value != null) {
            ps.setInt(index, value);
        } else {
            ps.setNull(index, Types.INTEGER);
        }
    }

    // ── Scheduler support methods ─────────────────────────────────────────────

    /**
     * Returns all ACTIVE schedules whose next_run_date is today or in the past.
     */
    public List<Schedule> getDueSchedules() {
        String sql = """
                SELECT schedule_id,
                       sender_account_number,
                       receiver_account_number,
                       amount,
                       currency_id,
                       payment_method_id,
                       description,
                       frequency,
                       start_date,
                       end_date,
                       next_run_date,
                       last_run_date,
                       status
                FROM Schedules
                WHERE status = 'ACTIVE'
                  AND next_run_date <= CURDATE()
                """;

        return jdbcTemplate.query(sql, scheduleRowMapper);
    }

    /**
     * Advances last_run_date and next_run_date after a successful execution.
     */
    public void updateNextExecution(Long scheduleId, java.sql.Date lastRun, java.sql.Date nextRun) {
        String sql = """
                UPDATE Schedules
                SET last_run_date = ?,
                    next_run_date = ?
                WHERE schedule_id = ?
                """;

        jdbcTemplate.update(sql, lastRun, nextRun, scheduleId);
    }

    /**
     * Marks a schedule COMPLETED (end_date reached or one-shot finished).
     */
    public void markCompleted(Long scheduleId) {
        String sql = """
                UPDATE Schedules
                SET status = 'COMPLETED'
                WHERE schedule_id = ?
                """;

        jdbcTemplate.update(sql, scheduleId);
    }

    public List<Schedule> getAllSchedules() {
        String sql = """
                SELECT *
                FROM Schedules
                ORDER BY schedule_id
                """;

        return jdbcTemplate.query(sql, scheduleRowMapper);
    }

    public boolean paymentMethodExists(Integer paymentMethodId) {
        String sql = """
                SELECT COUNT(*)
                FROM PaymentMethods
                WHERE payment_method_id = ?
                """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, paymentMethodId);
        return count != null && count > 0;
    }
}

