package com.sitepark.ies.application.audit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.audit.core.usecase.RevertActionsUseCase;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RevertActionsServiceTest {

  private RevertActionsUseCase revertActionsUseCase;

  @SuppressWarnings("PMD.SingularField")
  private ApplicationAuditLogServiceFactory auditLogServiceFactory;

  private ApplicationAuditLogService auditLogService;
  private RevertActionsService service;

  @BeforeEach
  void setUp() {
    this.revertActionsUseCase = mock();
    this.auditLogServiceFactory = mock();
    this.auditLogService = mock();
    Clock clock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC);
    this.service =
        new RevertActionsService(this.revertActionsUseCase, this.auditLogServiceFactory, clock);
    when(this.auditLogServiceFactory.create(any(), any())).thenReturn(this.auditLogService);
    when(this.auditLogService.createBatchLog(any(), any())).thenReturn("batch-id");
  }

  @Test
  void testRevertCallsRevertActionsUseCase() {
    RevertActionsServiceRequest request =
        new RevertActionsServiceRequest(List.of("log-id-1"), null);
    this.service.revert(request);
    verify(this.revertActionsUseCase).revert(any());
  }

  @Test
  void testRevertWithSingleIdDoesNotCreateBatchLog() {
    RevertActionsServiceRequest request =
        new RevertActionsServiceRequest(List.of("log-id-1"), null);
    this.service.revert(request);
    verify(this.auditLogService, never()).createBatchLog(any(), any());
  }

  @Test
  void testRevertWithMultipleIdsCreatesBatchLog() {
    RevertActionsServiceRequest request =
        new RevertActionsServiceRequest(List.of("log-id-1", "log-id-2"), null);
    this.service.revert(request);
    verify(this.auditLogService).createBatchLog(any(), any());
  }
}
