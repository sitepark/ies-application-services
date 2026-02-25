package com.sitepark.ies.application.audit.revert.user;

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
import com.sitepark.ies.application.user.AssignRolesToUsersService;
import com.sitepark.ies.application.user.UnassignRolesFromUsersService;
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

class RevertBatchReassignRolesToUsersActionHandlerTest {

  @SuppressWarnings("PMD.SingularField")
  private ApplicationAuditLogServiceFactory auditLogServiceFactory;

  private AuditLogService auditLogService;
  private AssignRolesToUsersService assignRolesToUsersService;
  private UnassignRolesFromUsersService unassignRolesFromUsersService;

  @SuppressWarnings("PMD.SingularField")
  private Clock clock;

  private RevertRequest request;
  private RevertBatchReassignRolesToUsersActionHandler handler;

  @BeforeEach
  void setUp() {
    this.auditLogServiceFactory = mock();
    this.auditLogService = mock();
    this.assignRolesToUsersService = mock();
    this.unassignRolesFromUsersService = mock();
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
        new RevertBatchReassignRolesToUsersActionHandler(
            this.auditLogServiceFactory,
            this.auditLogService,
            this.assignRolesToUsersService,
            this.unassignRolesFromUsersService,
            this.clock);
  }

  @Test
  void testRevertWithEmptyChildIdsDoesNotCallService() {
    when(this.auditLogService.getRecursiveChildIds("log-id")).thenReturn(List.of());

    this.handler.revert(this.request);

    verify(this.assignRolesToUsersService, never()).assignRolesToUsers(any());
  }

  @Test
  void testRevertWithAssignActionCallsUnassignService() throws IOException {
    when(this.auditLogService.getRecursiveChildIds("log-id")).thenReturn(List.of("child-1"));

    AuditLog auditLog = mock();
    when(auditLog.entityId()).thenReturn("1");
    when(auditLog.backwardData()).thenReturn("{}");
    when(auditLog.action()).thenReturn(AuditLogAction.ASSIGN_ROLES_TO_USERS.name());
    when(this.auditLogService.getAuditLog("child-1")).thenReturn(Optional.of(auditLog));
    when(this.auditLogService.deserializeList(eq("{}"), eq(String.class)))
        .thenReturn(List.of("101"));

    this.handler.revert(this.request);

    verify(this.unassignRolesFromUsersService).unassignRolesFromUsers(any());
  }

  @Test
  void testRevertWithUnassignActionCallsAssignService() throws IOException {
    when(this.auditLogService.getRecursiveChildIds("log-id")).thenReturn(List.of("child-1"));

    AuditLog auditLog = mock();
    when(auditLog.entityId()).thenReturn("1");
    when(auditLog.backwardData()).thenReturn("{}");
    when(auditLog.action()).thenReturn(AuditLogAction.UNASSIGN_ROLES_FROM_USERS.name());
    when(this.auditLogService.getAuditLog("child-1")).thenReturn(Optional.of(auditLog));
    when(this.auditLogService.deserializeList(eq("{}"), eq(String.class)))
        .thenReturn(List.of("101"));

    this.handler.revert(this.request);

    verify(this.assignRolesToUsersService).assignRolesToUsers(any());
  }

  @Test
  void testRevertWithUnknownActionThrowsRevertFailedException() throws IOException {
    when(this.auditLogService.getRecursiveChildIds("log-id")).thenReturn(List.of("child-1"));

    AuditLog auditLog = mock();
    when(auditLog.entityId()).thenReturn("1");
    when(auditLog.backwardData()).thenReturn("{}");
    when(auditLog.action()).thenReturn("UNKNOWN_ACTION");
    when(this.auditLogService.getAuditLog("child-1")).thenReturn(Optional.of(auditLog));
    when(this.auditLogService.deserializeList(eq("{}"), eq(String.class)))
        .thenReturn(List.of("101"));

    assertThrows(
        RevertFailedException.class,
        () -> this.handler.revert(this.request),
        "revert() should throw RevertFailedException when the audit log action is unknown");
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
        "revert() should throw RevertFailedException when deserialization of roleIds fails");
  }
}
