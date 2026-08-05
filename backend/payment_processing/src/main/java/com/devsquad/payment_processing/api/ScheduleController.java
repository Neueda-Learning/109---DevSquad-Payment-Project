package com.devsquad.payment_processing.api;

import com.devsquad.payment_processing.model.Schedule;
import com.devsquad.payment_processing.service.ScheduleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/schedules")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    /**
     * Create a new scheduled payment
     */
    @GetMapping("/all")
    public List<Schedule> getAllSchedules() {
        return scheduleService.getAllSchedules();
    }
    @PostMapping
    public Schedule createSchedule(
            @Valid @RequestBody Schedule request) {
        return scheduleService.createSchedule(request);
    }

    @GetMapping("/{scheduleId}")
    public Schedule getScheduleById(
            @PathVariable Integer scheduleId) {
        return scheduleService.getScheduleById(scheduleId);
    }

    @PatchMapping("/{scheduleId}")
    public String updateSchedule(
            @PathVariable Long scheduleId,
            @Valid @RequestBody Schedule request) {
        scheduleService.updateSchedule(scheduleId, request);
        return "Schedule updated successfully";
    }

    @DeleteMapping("/{scheduleId}")
    public String deleteSchedule(
            @PathVariable Integer scheduleId) {
        scheduleService.deleteSchedule(scheduleId);
        return "Schedule deleted successfully";
    }

    @PostMapping("/{scheduleId}/trigger")
    public String triggerSchedule(
            @PathVariable Long scheduleId) {
        scheduleService.triggerSchedule(scheduleId);
        return "Scheduled payment triggered successfully";
    }

    /**
     * Get execution history/status of a schedule
     */
//    @GetMapping("/{scheduleId}/execution")
//    public Object getScheduleExecution(
//            @PathVariable Long scheduleId) {
//        System.out.println("Raised a requestto execute schedule with id: " + scheduleId);
//        return scheduleService.getScheduleExecution(scheduleId);
//    }

}