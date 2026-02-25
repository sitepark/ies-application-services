package com.sitepark.ies.application.audit.revert.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.user.RemoveUsersService;
import com.sitepark.ies.audit.core.domain.value.AuditLogTarget;
import com.sitepark.ies.audit.core.service.RevertRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RevertCreateUserActionHandlerTest {

  private RemoveUsersService removeUserService;
  private RevertRequest request;
  private RevertCreateUserActionHandler handler;

  @BeforeEach
  void setUp() {
    this.removeUserService = mock();
    this.request = mock();
    AuditLogTarget target = mock();
    when(this.request.target()).thenReturn(target);
    when(target.id()).thenReturn("1");
    when(this.request.parentId()).thenReturn("parent-id");
    this.handler = new RevertCreateUserActionHandler(this.removeUserService);
  }

  @Test
  void testRevertCallsRemoveUsersService() {
    this.handler.revert(this.request);
    verify(this.removeUserService).removeUsers(any());
  }
}
