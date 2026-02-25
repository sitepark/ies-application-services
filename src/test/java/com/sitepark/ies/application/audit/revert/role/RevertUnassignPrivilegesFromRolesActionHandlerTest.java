package com.sitepark.ies.application.audit.revert.role;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.application.role.AssignPrivilegesToRolesService;
import com.sitepark.ies.audit.core.domain.value.AuditLogTarget;
import com.sitepark.ies.audit.core.service.AuditLogService;
import com.sitepark.ies.audit.core.service.RevertRequest;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RevertUnassignPrivilegesFromRolesActionHandlerTest {

  private AuditLogService auditLogService;
  private AssignPrivilegesToRolesService assignPrivilegesToRolesService;
  private RevertRequest request;
  private RevertUnassignPrivilegesFromRolesActionHandler handler;

  @BeforeEach
  void setUp() {
    this.auditLogService = mock();
    this.assignPrivilegesToRolesService = mock();
    this.request = mock();

    AuditLogTarget target = mock();
    when(this.request.target()).thenReturn(target);
    when(target.id()).thenReturn("1");
    when(this.request.parentId()).thenReturn("parent-id");
    when(this.request.backwardData()).thenReturn("{}");

    this.handler =
        new RevertUnassignPrivilegesFromRolesActionHandler(
            this.auditLogService, this.assignPrivilegesToRolesService);
  }

  @Test
  void testRevertCallsAssignPrivilegesToRolesService() throws IOException {
    when(this.auditLogService.deserializeList(eq("{}"), eq(String.class))).thenReturn(List.of("1"));

    this.handler.revert(this.request);

    verify(this.assignPrivilegesToRolesService).assignPrivilegesToRoles(any());
  }

  @Test
  void testRevertWithDeserializationFailureThrowsRevertFailedException() throws IOException {
    when(this.auditLogService.deserializeList(eq("{}"), eq(String.class)))
        .thenThrow(new IOException("deserialization error"));

    assertThrows(
        RevertFailedException.class,
        () -> this.handler.revert(this.request),
        "revert() should throw RevertFailedException when deserialization of privilegeIds fails");
  }
}
