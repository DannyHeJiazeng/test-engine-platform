package com.test.web.service;

import com.test.web.entity.InspectionTaskEntity;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import static org.mockito.Mockito.*;

class PersistentInspectionSchedulerTest {
    @Test
    void runsPersistedDueTasksOnly() {
        var service = mock(InspectionService.class);
        var due = new InspectionTaskEntity();
        due.setTaskId("restored");
        due.setIntervalSeconds(300);
        var waiting = new InspectionTaskEntity();
        waiting.setTaskId("waiting");
        waiting.setIntervalSeconds(300);
        waiting.setLastRunTime(LocalDateTime.now());
        when(service.getEnabledTasks()).thenReturn(List.of(due, waiting));
        new PersistentInspectionScheduler(service).executeDueTasks();
        verify(service).executeTask("restored");
        verify(service, never()).executeTask("waiting");
    }
}
