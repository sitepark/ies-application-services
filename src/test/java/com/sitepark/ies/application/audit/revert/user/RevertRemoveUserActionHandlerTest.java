package com.sitepark.ies.application.audit.revert.user;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.audit.core.domain.value.AuditLogTarget;
import com.sitepark.ies.audit.core.service.AuditLogService;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import com.sitepark.ies.userrepository.core.domain.value.UserSnapshot;
import com.sitepark.ies.userrepository.core.usecase.user.RestoreUserResult;
import com.sitepark.ies.userrepository.core.usecase.user.RestoreUserUseCase;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RevertRemoveUserActionHandlerTest {

  @SuppressWarnings("PMD.SingularField")
  private ApplicationAuditLogServiceFactory auditLogServiceFactory;

  private AuditLogService auditLogService;
  private RestoreUserUseCase restoreUserUseCase;
  private RevertRequest request;
  private RevertRemoveUserActionHandler handler;

  @BeforeEach
  void setUp() {
    this.auditLogServiceFactory = mock();
    this.auditLogService = mock();
    this.restoreUserUseCase = mock();
    this.request = mock();

    AuditLogTarget target = mock();
    when(this.request.target()).thenReturn(target);
    when(target.id()).thenReturn("1");
    when(this.request.parentId()).thenReturn("parent-id");
    when(this.request.backwardData()).thenReturn("{}");

    ApplicationAuditLogService mockAppAuditLogService = mock();
    when(this.auditLogServiceFactory.create(any(), any())).thenReturn(mockAppAuditLogService);

    this.handler =
        new RevertRemoveUserActionHandler(
            this.auditLogServiceFactory, this.auditLogService, this.restoreUserUseCase);
  }

  @Test
  void testRevertCallsRestoreUserUseCase() throws IOException {
    User user = User.builder().id("1").login("testuser").lastName("Test").build();
    UserSnapshot snapshot = new UserSnapshot(user, List.of());
    RestoreUserResult.Restored restored =
        new RestoreUserResult.Restored("1", snapshot, Instant.now());

    when(this.auditLogService.deserialize("{}", UserSnapshot.class)).thenReturn(snapshot);
    when(this.restoreUserUseCase.restoreUser(any())).thenReturn(restored);

    this.handler.revert(this.request);

    verify(this.restoreUserUseCase).restoreUser(any());
  }

  @Test
  void testRevertWithDeserializationFailureThrowsRevertFailedException() throws IOException {
    when(this.auditLogService.deserialize("{}", UserSnapshot.class))
        .thenThrow(new IOException("deserialization error"));

    assertThrows(
        RevertFailedException.class,
        () -> this.handler.revert(this.request),
        "revert() should throw RevertFailedException when deserialization of user-snapshot fails");
  }
}
