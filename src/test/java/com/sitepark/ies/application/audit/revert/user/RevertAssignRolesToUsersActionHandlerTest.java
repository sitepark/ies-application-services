package com.sitepark.ies.application.audit.revert.user;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.application.user.UnassignRolesFromUsersService;
import com.sitepark.ies.audit.core.domain.value.AuditLogTarget;
import com.sitepark.ies.audit.core.service.AuditLogService;
import com.sitepark.ies.audit.core.service.RevertRequest;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RevertAssignRolesToUsersActionHandlerTest {

  private AuditLogService auditLogService;
  private UnassignRolesFromUsersService unassignRolesFromUsersService;
  private RevertRequest request;
  private RevertAssignRolesToUsersActionHandler handler;

  @BeforeEach
  void setUp() {
    this.auditLogService = mock();
    this.unassignRolesFromUsersService = mock();
    this.request = mock();

    AuditLogTarget target = mock();
    when(this.request.target()).thenReturn(target);
    when(target.id()).thenReturn("1");
    when(this.request.parentId()).thenReturn("parent-id");
    when(this.request.backwardData()).thenReturn("{}");

    this.handler =
        new RevertAssignRolesToUsersActionHandler(
            this.auditLogService, this.unassignRolesFromUsersService);
  }

  @Test
  void testRevertCallsUnassignRolesFromUsersService() throws IOException {
    when(this.auditLogService.deserializeList(eq("{}"), eq(String.class))).thenReturn(List.of("1"));

    this.handler.revert(this.request);

    verify(this.unassignRolesFromUsersService).unassignRolesFromUsers(any());
  }

  @Test
  void testRevertWithDeserializationFailureThrowsRevertFailedException() throws IOException {
    when(this.auditLogService.deserializeList(eq("{}"), eq(String.class)))
        .thenThrow(new IOException("deserialization error"));

    assertThrows(
        RevertFailedException.class,
        () -> this.handler.revert(this.request),
        "revert() should throw RevertFailedException when deserialization of roleIds fails");
  }
}
