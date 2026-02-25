package com.sitepark.ies.application.audit.revert.role;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.audit.core.domain.value.AuditLogTarget;
import com.sitepark.ies.audit.core.service.AuditLogService;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.userrepository.core.domain.value.RoleSnapshot;
import com.sitepark.ies.userrepository.core.usecase.role.RestoreRoleUseCase;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RevertRemoveRoleActionHandlerTest {

  private ApplicationAuditLogServiceFactory auditLogServiceFactory;
  private AuditLogService auditLogService;
  private RestoreRoleUseCase restoreRoleUseCase;
  private RevertRequest request;
  private RevertRemoveRoleActionHandler handler;

  @BeforeEach
  void setUp() {
    this.auditLogServiceFactory = mock();
    this.auditLogService = mock();
    this.restoreRoleUseCase = mock();
    this.request = mock();

    AuditLogTarget target = mock();
    when(this.request.target()).thenReturn(target);
    when(target.id()).thenReturn("1");
    when(this.request.parentId()).thenReturn("parent-id");
    when(this.request.backwardData()).thenReturn("{}");

    this.handler =
        new RevertRemoveRoleActionHandler(
            this.auditLogServiceFactory, this.auditLogService, this.restoreRoleUseCase);
  }

  @Test
  void testRevertCallsRestoreRoleUseCase() throws IOException {
    RoleSnapshot roleSnapshot = mock();
    when(this.auditLogService.deserialize(eq("{}"), eq(RoleSnapshot.class)))
        .thenReturn(roleSnapshot);

    this.handler.revert(this.request);

    verify(this.restoreRoleUseCase).restoreRole(any());
  }

  @Test
  void testRevertWithDeserializationFailureThrowsRevertFailedException() throws IOException {
    when(this.auditLogService.deserialize(eq("{}"), eq(RoleSnapshot.class)))
        .thenThrow(new IOException("deserialization error"));

    assertThrows(
        RevertFailedException.class,
        () -> this.handler.revert(this.request),
        "revert() should throw RevertFailedException when deserialization of role-snapshot fails");
  }
}
