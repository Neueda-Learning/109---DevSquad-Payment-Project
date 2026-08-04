package com.devsquad.payment_processing.service;

import com.devsquad.payment_processing.model.Schedule;
import com.devsquad.payment_processing.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepo;

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

    // Trigger Schedule Manually
    public void triggerSchedule(Long scheduleId) {
        scheduleRepo.triggerSchedule(scheduleId);
    }

    // Get Schedule Execution Details
    public Map<String, Object> getScheduleExecution(Long scheduleId) {
        return scheduleRepo.getScheduleExecution(scheduleId);
    }
}