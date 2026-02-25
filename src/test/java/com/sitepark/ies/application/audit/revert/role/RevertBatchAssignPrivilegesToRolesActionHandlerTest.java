package com.sitepark.ies.application.audit.revert.role;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.application.role.UnassignPrivilegesFromRolesService;
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

class RevertBatchAssignPrivilegesToRolesActionHandlerTest {

  private ApplicationAuditLogServiceFactory auditLogServiceFactory;
  private AuditLogService auditLogService;
  private UnassignPrivilegesFromRolesService unassignPrivilegesFromRolesService;
  private Clock clock;
  private RevertRequest request;
  private RevertBatchAssignPrivilegesToRolesActionHandler handler;

  @BeforeEach
  void setUp() {
    this.auditLogServiceFactory = mock();
    this.auditLogService = mock();
    this.unassignPrivilegesFromRolesService = mock();
    this.clock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC);
    this.request = mock();

    AuditLogTarget target = mock();
    when(this.request.target()).thenReturn(target);
    when(target.id()).thenReturn("1");
    when(this.request.parentId()).thenReturn("parent-id");
    when(this.request.backwardData()).thenReturn("{}");
    when(this.request.id()).thenReturn("log-id");

    ApplicationAuditLogService mockAppAuditLogService = mock();
    when(this.auditLogServiceFactory.create(any(), any())).thenReturn(mockAppAuditLogService);
    when(mockAppAuditLogService.createBatchLog(any(), any())).thenReturn("batch-id");
    when(mockAppAuditLogService.parentId()).thenReturn("batch-id");

    this.handler =
        new RevertBatchAssignPrivilegesToRolesActionHandler(
            this.auditLogServiceFactory,
            this.auditLogService,
            this.unassignPrivilegesFromRolesService,
            this.clock);
  }

  @Test
  void testRevertWithEmptyChildIdsDoesNotCallUnassignService() {
    when(this.auditLogService.getRecursiveChildIds("log-id")).thenReturn(List.of());

    this.handler.revert(this.request);

    verify(this.unassignPrivilegesFromRolesService, never()).unassignPrivilegesFromRoles(any());
  }

  @Test
  void testRevertCallsUnassignPrivilegesFromRolesServiceForEachChild() throws IOException {
    when(this.auditLogService.getRecursiveChildIds("log-id")).thenReturn(List.of("child-1"));

    AuditLog auditLog = mock();
    when(auditLog.entityId()).thenReturn("1");
    when(auditLog.backwardData()).thenReturn("{}");
    when(this.auditLogService.getAuditLog("child-1")).thenReturn(Optional.of(auditLog));
    when(this.auditLogService.deserializeList(eq("{}"), eq(String.class))).thenReturn(List.of("1"));

    this.handler.revert(this.request);

    verify(this.unassignPrivilegesFromRolesService).unassignPrivilegesFromRoles(any());
  }

  @Test
  void testRevertWithDeserializationFailureThrowsRevertFailedException() throws IOException {
    when(this.auditLogService.getRecursiveChildIds("log-id")).thenReturn(List.of("child-1"));

    AuditLog auditLog = mock();
    when(auditLog.entityId()).thenReturn("1");
    when(auditLog.backwardData()).thenReturn("{}");
    when(this.auditLogService.getAuditLog("child-1")).thenReturn(Optional.of(auditLog));
    when(this.auditLogService.deserializeList(eq("{}"), eq(String.class)))
        .thenThrow(new IOException("deserialization error"));

    assertThrows(
        RevertFailedException.class,
        () -> this.handler.revert(this.request),
        "revert() should throw RevertFailedException when deserialization of privilegeIds fails");
  }
}
