package com.devsquad.payment_processing.repository;

import com.devsquad.payment_processing.model.Frequency;
import com.devsquad.payment_processing.model.Schedule;
import com.devsquad.payment_processing.model.ScheduleStatus;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ScheduleRowMapper implements RowMapper<Schedule> {

    @Override
    public Schedule mapRow(ResultSet rs, int rowNum) throws SQLException {
        Long rawId = rs.getObject("schedule_id", Long.class);
        String frequencyStr = rs.getString("frequency");
        
        return new Schedule(
                rawId != null ? rawId.intValue() : null,
                rs.getLong("sender_account_number"),
                rs.getLong("receiver_account_number"),
                rs.getBigDecimal("amount"),
                rs.getObject("currency_id", Integer.class),
                rs.getObject("payment_method_id", Integer.class),
                rs.getString("description"),
                frequencyStr != null ? Frequency.valueOf(frequencyStr) : null,
                rs.getDate("start_date"),
                rs.getDate("end_date"),
                rs.getDate("next_run_date"),
                rs.getDate("last_run_date"),
                ScheduleStatus.valueOf(rs.getString("status"))
        );
    }
}

