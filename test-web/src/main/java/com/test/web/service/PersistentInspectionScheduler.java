package com.test.web.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class PersistentInspectionScheduler {
    private final InspectionService inspectionService;

    @Scheduled(fixedDelay = 1000, initialDelay = 1000)
    public void executeDueTasks() {
        for (var task : inspectionService.getEnabledTasks()) {
            if (task.getLastRunTime() == null ||
                    !task.getLastRunTime().plusSeconds(task.getIntervalSeconds()).isAfter(LocalDateTime.now())) {
                try {
                    inspectionService.executeTask(task.getTaskId());
                } catch (Exception e) {
                    log.error("Scheduled inspection failed: {}", task.getTaskId(), e);
                }
            }
        }
    }
}
