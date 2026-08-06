package com.devsquad.payment_processing.api;

import com.devsquad.payment_processing.service.BatchScheduleService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/batch-schedules")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class BatchScheduleController {

    private final BatchScheduleService batchScheduleService;

    public BatchScheduleController(BatchScheduleService batchScheduleService) {
        this.batchScheduleService = batchScheduleService;
    }

    @GetMapping
    public List<Map<String, Object>> getAllBatchSchedules() {
        return batchScheduleService.getAllBatchSchedules();
    }

    @GetMapping("/{batchId}")
    public Map<String, Object> getBatchScheduleDetails(@PathVariable String batchId) {
        return batchScheduleService.getBatchScheduleDetails(batchId);
    }
}

