package com.devsquad.payment_processing.service;

import com.devsquad.payment_processing.Model.Schedule;
import com.devsquad.payment_processing.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public Schedule updateSchedule(Schedule schedule) {
        return scheduleRepo.updateSchedule(schedule);
    }

    // Delete Schedule
    public void deleteSchedule(Integer scheduleId) {
        scheduleRepo.deleteSchedule(scheduleId);
    }

//    // Trigger Schedule Manually
//    public void triggerSchedule(Integer scheduleId) {
//        scheduleRepo.triggerSchedule(scheduleId);
//    }
//
//    // Get Schedule Execution Details
//    public Schedule getScheduleExecution(Integer scheduleId) {
//        return scheduleRepo.getScheduleExecution(scheduleId);
//    }
}