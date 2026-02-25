package com.sitepark.ies.application.audit.revert.role;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.audit.core.domain.value.AuditLogTarget;
import com.sitepark.ies.audit.core.service.AuditLogService;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.userrepository.core.domain.entity.Role;
import com.sitepark.ies.userrepository.core.domain.value.RoleSnapshot;
import com.sitepark.ies.userrepository.core.usecase.role.RestoreRoleUseCase;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RevertBatchRemoveRolesActionHandlerTest {

  private ApplicationAuditLogServiceFactory auditLogServiceFactory;
  private AuditLogService auditLogService;
  private RestoreRoleUseCase restoreRoleUseCase;
  private Clock clock;
  private RevertRequest request;
  private RevertBatchRemoveRolesActionHandler handler;

  @BeforeEach
  void setUp() {
    this.auditLogServiceFactory = mock();
    this.auditLogService = mock();
    this.restoreRoleUseCase = mock();
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
        new RevertBatchRemoveRolesActionHandler(
            this.auditLogServiceFactory, this.auditLogService, this.restoreRoleUseCase, this.clock);
  }

  @Test
  void testRevertWithEmptyChildIdsDoesNotCallRestoreRoleUseCase() {
    when(this.auditLogService.getRecursiveChildIds("log-id")).thenReturn(List.of());

    this.handler.revert(this.request);

    verify(this.restoreRoleUseCase, never()).restoreRole(any());
  }

  @Test
  void testRevertCallsRestoreRoleUseCaseForEachChild() throws IOException {
    when(this.auditLogService.getRecursiveChildIds("log-id")).thenReturn(List.of("child-1"));

    Role role = mock();
    when(role.id()).thenReturn("1");
    when(role.name()).thenReturn("test-role");
    RoleSnapshot roleSnapshot = mock();
    when(roleSnapshot.role()).thenReturn(role);
    when(this.auditLogService.getBackwardData("child-1", RoleSnapshot.class))
        .thenReturn(Optional.of(roleSnapshot));

    this.handler.revert(this.request);

    verify(this.restoreRoleUseCase).restoreRole(any());
  }

  @Test
  void testRevertWithMissingBackwardDataThrowsRevertFailedException() throws IOException {
    when(this.auditLogService.getRecursiveChildIds("log-id")).thenReturn(List.of("child-1"));
    when(this.auditLogService.getBackwardData("child-1", RoleSnapshot.class))
        .thenReturn(Optional.empty());

    assertThrows(
        RevertFailedException.class,
        () -> this.handler.revert(this.request),
        "revert() should throw RevertFailedException when backward data is missing for a child"
            + " log");
  }

  @Test
  void testRevertWithDeserializationFailureThrowsRevertFailedException() throws IOException {
    when(this.auditLogService.getRecursiveChildIds("log-id")).thenReturn(List.of("child-1"));
    when(this.auditLogService.getBackwardData("child-1", RoleSnapshot.class))
        .thenThrow(new IOException("deserialization error"));

    assertThrows(
        RevertFailedException.class,
        () -> this.handler.revert(this.request),
        "revert() should throw RevertFailedException when deserialization of role-snapshot fails");
  }
}
