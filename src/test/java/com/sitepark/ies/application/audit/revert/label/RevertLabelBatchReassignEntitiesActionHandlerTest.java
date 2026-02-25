package com.sitepark.ies.application.audit.revert.label;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.application.label.AssignLabelsToEntitiesService;
import com.sitepark.ies.application.label.UnassignLabelsFromEntitiesService;
import com.sitepark.ies.audit.core.domain.entity.AuditLog;
import com.sitepark.ies.audit.core.domain.value.AuditLogTarget;
import com.sitepark.ies.audit.core.service.AuditLogService;
import com.sitepark.ies.audit.core.service.RevertRequest;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RevertLabelBatchReassignEntitiesActionHandlerTest {

  private ApplicationAuditLogServiceFactory auditLogServiceFactory;
  private AuditLogService auditLogService;
  private AssignLabelsToEntitiesService assignLabelsToEntitiesService;
  private UnassignLabelsFromEntitiesService unassignLabelsFromEntitiesService;
  private Clock clock;
  private RevertRequest request;
  private RevertLabelBatchReassignEntitiesActionHandler handler;

  @BeforeEach
  void setUp() {
    this.auditLogServiceFactory = mock();
    this.auditLogService = mock();
    this.assignLabelsToEntitiesService = mock();
    this.unassignLabelsFromEntitiesService = mock();
    this.clock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC);
    this.request = mock();

    AuditLogTarget target = mock();
    when(this.request.target()).thenReturn(target);
    when(target.id()).thenReturn("1");
    when(this.request.parentId()).thenReturn("parent-id");
    when(this.request.id()).thenReturn("log-id");

    ApplicationAuditLogService mockAppAuditLogService = mock();
    when(this.auditLogServiceFactory.create(any(), any())).thenReturn(mockAppAuditLogService);
    when(mockAppAuditLogService.createBatchLog(any(), any())).thenReturn("batch-id");
    when(mockAppAuditLogService.parentId()).thenReturn("batch-id");

    this.handler =
        new RevertLabelBatchReassignEntitiesActionHandler(
            this.auditLogServiceFactory,
            this.auditLogService,
            this.assignLabelsToEntitiesService,
            this.unassignLabelsFromEntitiesService,
            this.clock);
  }

  @Test
  void testRevertWithEmptyChildIdsDoesNotCallService() {
    when(this.auditLogService.getRecursiveChildIds("log-id")).thenReturn(List.of());

    this.handler.revert(this.request);

    verify(this.unassignLabelsFromEntitiesService, never()).unassignLabelsFromEntities(any());
  }

  @Test
  void testRevertWithAssignActionCallsUnassignService() throws IOException {
    when(this.auditLogService.getRecursiveChildIds("log-id")).thenReturn(List.of("child-1"));

    AuditLog auditLog = mock();
    when(auditLog.entityType()).thenReturn("LabelEntity");
    when(auditLog.entityId()).thenReturn("1");
    when(auditLog.backwardData()).thenReturn("{}");
    when(auditLog.action()).thenReturn(AuditLogAction.ASSIGN_LABELS_TO_ENTITIES.name());
    when(this.auditLogService.getAuditLog("child-1")).thenReturn(Optional.of(auditLog));
    when(this.auditLogService.deserializeList(eq("{}"), eq(String.class))).thenReturn(List.of("1"));

    this.handler.revert(this.request);

    verify(this.unassignLabelsFromEntitiesService).unassignLabelsFromEntities(any());
  }

  @Test
  void testRevertWithUnassignActionCallsAssignService() throws IOException {
    when(this.auditLogService.getRecursiveChildIds("log-id")).thenReturn(List.of("child-1"));

    AuditLog auditLog = mock();
    when(auditLog.entityType()).thenReturn("LabelEntity");
    when(auditLog.entityId()).thenReturn("1");
    when(auditLog.backwardData()).thenReturn("{}");
    when(auditLog.action()).thenReturn(AuditLogAction.UNASSIGN_LABELS_FROM_ENTITIES.name());
    when(this.auditLogService.getAuditLog("child-1")).thenReturn(Optional.of(auditLog));
    when(this.auditLogService.deserializeList(eq("{}"), eq(String.class))).thenReturn(List.of("1"));

    this.handler.revert(this.request);

    verify(this.assignLabelsToEntitiesService).assignLabelsToEntities(any());
  }

  @Test
  void testRevertWithUnknownActionThrowsRevertFailedException() throws IOException {
    when(this.auditLogService.getRecursiveChildIds("log-id")).thenReturn(List.of("child-1"));

    AuditLog auditLog = mock();
    when(auditLog.entityType()).thenReturn("LabelEntity");
    when(auditLog.entityId()).thenReturn("1");
    when(auditLog.backwardData()).thenReturn("{}");
    when(auditLog.action()).thenReturn("UNKNOWN_ACTION");
    when(this.auditLogService.getAuditLog("child-1")).thenReturn(Optional.of(auditLog));
    when(this.auditLogService.deserializeList(eq("{}"), eq(String.class))).thenReturn(List.of("1"));

    assertThrows(
        RevertFailedException.class,
        () -> this.handler.revert(this.request),
        "revert() should throw RevertFailedException when auditLog action is unknown");
  }
}
