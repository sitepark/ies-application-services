package com.sitepark.ies.application.audit.revert.role;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.role.RemoveRolesService;
import com.sitepark.ies.audit.core.domain.value.AuditLogTarget;
import com.sitepark.ies.audit.core.service.RevertRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RevertCreateRoleActionHandlerTest {

  private RemoveRolesService removeRolesService;
  private RevertRequest request;
  private RevertCreateRoleActionHandler handler;

  @BeforeEach
  void setUp() {
    this.removeRolesService = mock();
    this.request = mock();

    AuditLogTarget target = mock();
    when(this.request.target()).thenReturn(target);
    when(target.id()).thenReturn("1");
    when(this.request.parentId()).thenReturn("parent-id");

    this.handler = new RevertCreateRoleActionHandler(this.removeRolesService);
  }

  @Test
  void testRevertCallsRemoveRolesService() {
    this.handler.revert(this.request);

    verify(this.removeRolesService).removeRoles(any());
  }
}
