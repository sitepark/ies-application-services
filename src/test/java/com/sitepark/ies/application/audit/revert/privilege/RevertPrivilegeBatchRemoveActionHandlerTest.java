package com.sitepark.ies.application.audit.revert.privilege;

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
import com.sitepark.ies.userrepository.core.domain.entity.Privilege;
import com.sitepark.ies.userrepository.core.domain.value.PrivilegeSnapshot;
import com.sitepark.ies.userrepository.core.usecase.privilege.RestorePrivilegeUseCase;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RevertPrivilegeBatchRemoveActionHandlerTest {

  @SuppressWarnings("PMD.SingularField")
  private ApplicationAuditLogServiceFactory auditLogServiceFactory;

  private AuditLogService auditLogService;
  private RestorePrivilegeUseCase restorePrivilegeUseCase;

  @SuppressWarnings("PMD.SingularField")
  private Clock clock;

  private RevertRequest request;
  private RevertPrivilegeBatchRemoveActionHandler handler;

  @BeforeEach
  void setUp() {
    this.auditLogServiceFactory = mock();
    this.auditLogService = mock();
    this.restorePrivilegeUseCase = mock();
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
        new RevertPrivilegeBatchRemoveActionHandler(
            this.auditLogServiceFactory,
            this.auditLogService,
            this.restorePrivilegeUseCase,
            this.clock);
  }

  @Test
  void testRevertWithEmptyChildIdsDoesNotCallRestorePrivilegeUseCase() {
    when(this.auditLogService.getRecursiveChildIds("log-id")).thenReturn(List.of());

    this.handler.revert(this.request);

    verify(this.restorePrivilegeUseCase, never()).restorePrivilege(any());
  }

  @Test
  void testRevertCallsRestorePrivilegeUseCaseForEachChild() throws IOException {
    when(this.auditLogService.getRecursiveChildIds("log-id")).thenReturn(List.of("child-1"));

    Privilege privilege = mock();
    when(privilege.id()).thenReturn("1");
    when(privilege.name()).thenReturn("test-privilege");
    PrivilegeSnapshot privilegeSnapshot = mock();
    when(privilegeSnapshot.privilege()).thenReturn(privilege);
    when(this.auditLogService.getBackwardData("child-1", PrivilegeSnapshot.class))
        .thenReturn(Optional.of(privilegeSnapshot));

    this.handler.revert(this.request);

    verify(this.restorePrivilegeUseCase).restorePrivilege(any());
  }

  @Test
  void testRevertWithMissingBackwardDataThrowsRevertFailedException() throws IOException {
    when(this.auditLogService.getRecursiveChildIds("log-id")).thenReturn(List.of("child-1"));
    when(this.auditLogService.getBackwardData("child-1", PrivilegeSnapshot.class))
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
    when(this.auditLogService.getBackwardData("child-1", PrivilegeSnapshot.class))
        .thenThrow(new IOException("deserialization error"));

    assertThrows(
        RevertFailedException.class,
        () -> this.handler.revert(this.request),
        "revert() should throw RevertFailedException when deserialization of privilege-snapshot"
            + " fails");
  }
}
