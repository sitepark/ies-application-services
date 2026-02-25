package com.sitepark.ies.application.audit.revert.privilege;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.audit.core.domain.value.AuditLogTarget;
import com.sitepark.ies.audit.core.service.AuditLogService;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.userrepository.core.domain.value.PrivilegeSnapshot;
import com.sitepark.ies.userrepository.core.usecase.privilege.RestorePrivilegeUseCase;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RevertPrivilegeRemoveActionHandlerTest {

  @SuppressWarnings("PMD.SingularField")
  private ApplicationAuditLogServiceFactory auditLogServiceFactory;

  private AuditLogService auditLogService;
  private RestorePrivilegeUseCase restorePrivilegeUseCase;
  private RevertRequest request;
  private RevertPrivilegeRemoveActionHandler handler;

  @BeforeEach
  void setUp() {
    this.auditLogServiceFactory = mock();
    this.auditLogService = mock();
    this.restorePrivilegeUseCase = mock();
    this.request = mock();

    AuditLogTarget target = mock();
    when(this.request.target()).thenReturn(target);
    when(target.id()).thenReturn("1");
    when(this.request.parentId()).thenReturn("parent-id");
    when(this.request.backwardData()).thenReturn("{}");

    this.handler =
        new RevertPrivilegeRemoveActionHandler(
            this.auditLogServiceFactory, this.auditLogService, this.restorePrivilegeUseCase);
  }

  @Test
  void testRevertCallsRestorePrivilegeUseCase() throws IOException {
    PrivilegeSnapshot privilegeSnapshot = mock();
    when(this.auditLogService.deserialize("{}", PrivilegeSnapshot.class))
        .thenReturn(privilegeSnapshot);

    this.handler.revert(this.request);

    verify(this.restorePrivilegeUseCase).restorePrivilege(any());
  }

  @Test
  void testRevertWithDeserializationFailureThrowsRevertFailedException() throws IOException {
    when(this.auditLogService.deserialize("{}", PrivilegeSnapshot.class))
        .thenThrow(new IOException("deserialization error"));

    assertThrows(
        RevertFailedException.class,
        () -> this.handler.revert(this.request),
        "revert() should throw RevertFailedException when deserialization of privilege-snapshot"
            + " fails");
  }
}
