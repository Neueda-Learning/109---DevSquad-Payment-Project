package com.devsquad.payment_processing.service;

import com.devsquad.payment_processing.model.Schedule;
import com.devsquad.payment_processing.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepo;

    @Autowired
    private ScheduleExecutionService executionService;

    // Create Schedule
    public Schedule createSchedule(Schedule schedule) {
        return scheduleRepo.createSchedule(schedule);
    }

    // Get Schedule by ID
    public Schedule getScheduleById(Integer scheduleId) {
        return scheduleRepo.getScheduleById(scheduleId);
    }

    // Update Schedule
    public Schedule updateSchedule(Long scheduleId, Schedule schedule) {
        return scheduleRepo.updateSchedule(scheduleId, schedule);
    }

    // Delete Schedule
    public void deleteSchedule(Integer scheduleId) {
        scheduleRepo.deleteSchedule(scheduleId);
    }

    /**
     * Manual trigger — reuses the exact same execution logic as the background scheduler.
     * No code duplication.
     */
    public void triggerSchedule(Long scheduleId) {
        Schedule schedule = scheduleRepo.getScheduleById(scheduleId.intValue());
        if (schedule == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "SCHEDULE_NOT_FOUND: " + scheduleId);
        }
        executionService.executeSchedule(schedule);
    }

    /**
     * Returns schedule execution details.
     * Payment history is in the Payments table (via schedule_id link).
     */
    public Object getScheduleExecution(Long scheduleId) {
        return scheduleRepo.getScheduleExecution(scheduleId);
    }
}