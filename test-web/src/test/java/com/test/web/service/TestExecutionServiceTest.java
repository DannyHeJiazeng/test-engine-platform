package com.test.web.service;

import com.test.core.TestNGRunner;
import com.test.core.TestResult;
import com.test.middleware.QualityGate;
import com.test.web.entity.TestExecutionEntity;
import com.test.web.entity.TestResultDetailEntity;
import com.test.web.repository.TestExecutionRepository;
import com.test.web.repository.TestResultDetailRepository;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class TestExecutionServiceTest {
    @Test
    public void failedExecutionCommitsFailureRecord() {
        var executions = mock(TestExecutionRepository.class);
        var target = new TestExecutionService(mock(TestNGRunner.class), mock(QualityGate.class),
                executions, mock(TestResultDetailRepository.class));
        var manager = mock(org.springframework.transaction.PlatformTransactionManager.class);
        var status = mock(org.springframework.transaction.TransactionStatus.class);
        when(manager.getTransaction(any())).thenReturn(status);
        var factory = new org.springframework.aop.framework.ProxyFactory(target);
        factory.addAdvice(new org.springframework.transaction.interceptor.TransactionInterceptor(manager,
                new org.springframework.transaction.annotation.AnnotationTransactionAttributeSource()));
        var service = (TestExecutionService) factory.getProxy();
        var request = new com.test.web.dto.TestExecutionRequest();
        request.setTestClass("missing.DoesNotExist");
        assertThrows(com.test.web.exception.BusinessException.class, () -> service.executeTest(request));
        verify(manager).commit(status);
        verify(manager, never()).rollback(any());
        verify(executions, atLeastOnce()).save(argThat(e -> "FAILED".equals(e.getStatus())));
    }

    @Test
    public void historyIncludesFailureDetails() {
        var executions = mock(TestExecutionRepository.class);
        var details = mock(TestResultDetailRepository.class);
        var service = new TestExecutionService(mock(TestNGRunner.class), mock(QualityGate.class), executions, details);
        var execution = new TestExecutionEntity();
        execution.setExecutionId("EXEC-1");
        execution.setTotalTests(1);
        execution.setPassedTests(0);
        execution.setFailedTests(1);
        var detail = new TestResultDetailEntity();
        detail.setClassName("ExampleTest");
        detail.setMethodName("fails");
        detail.setStatus("FAILURE");
        detail.setFailureMessage("expected failure");
        detail.setStackTrace("failure stack");
        when(executions.findByExecutionId("EXEC-1")).thenReturn(Optional.of(execution));
        when(details.findByExecutionId("EXEC-1")).thenReturn(List.of(detail));
        var result = service.getExecutionDetail("EXEC-1").getResults().get(0);
        assertEquals(result.getStatus(), TestResult.TestStatus.FAILURE);
        assertEquals(result.getFailureMessage(), "expected failure");
        assertEquals(result.getStackTrace(), "failure stack");
    }
}
