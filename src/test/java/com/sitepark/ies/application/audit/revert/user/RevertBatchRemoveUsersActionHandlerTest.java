package com.sitepark.ies.application.audit.revert.user;

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
import com.sitepark.ies.userrepository.core.domain.entity.User;
import com.sitepark.ies.userrepository.core.domain.value.UserSnapshot;
import com.sitepark.ies.userrepository.core.usecase.user.RestoreUserResult;
import com.sitepark.ies.userrepository.core.usecase.user.RestoreUserUseCase;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RevertBatchRemoveUsersActionHandlerTest {

  @SuppressWarnings("PMD.SingularField")
  private ApplicationAuditLogServiceFactory auditLogServiceFactory;

  private AuditLogService auditLogService;
  private RestoreUserUseCase restoreUserUseCase;

  @SuppressWarnings("PMD.SingularField")
  private Clock clock;

  private RevertRequest request;
  private RevertBatchRemoveUsersActionHandler handler;

  @BeforeEach
  void setUp() {
    this.auditLogServiceFactory = mock();
    this.auditLogService = mock();
    this.restoreUserUseCase = mock();
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
        new RevertBatchRemoveUsersActionHandler(
            this.auditLogServiceFactory, this.auditLogService, this.restoreUserUseCase, this.clock);
  }

  @Test
  void testRevertWithEmptyChildIdsDoesNotCallRestoreUserUseCase() {
    when(this.auditLogService.getRecursiveChildIds("log-id")).thenReturn(List.of());

    this.handler.revert(this.request);

    verify(this.restoreUserUseCase, never()).restoreUser(any());
  }

  @Test
  void testRevertCallsRestoreUserUseCaseForEachChild() throws IOException {
    when(this.auditLogService.getRecursiveChildIds("log-id")).thenReturn(List.of("child-1"));

    User user = User.builder().id("1").login("testuser").lastName("Test").build();
    UserSnapshot userSnapshot = new UserSnapshot(user, List.of());
    when(this.auditLogService.getBackwardData("child-1", UserSnapshot.class))
        .thenReturn(Optional.of(userSnapshot));

    RestoreUserResult.Restored restored =
        new RestoreUserResult.Restored("1", userSnapshot, Instant.now());
    when(this.restoreUserUseCase.restoreUser(any())).thenReturn(restored);

    this.handler.revert(this.request);

    verify(this.restoreUserUseCase).restoreUser(any());
  }

  @Test
  void testRevertWithMissingBackwardDataThrowsRevertFailedException() throws IOException {
    when(this.auditLogService.getRecursiveChildIds("log-id")).thenReturn(List.of("child-1"));
    when(this.auditLogService.getBackwardData("child-1", UserSnapshot.class))
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
    when(this.auditLogService.getBackwardData("child-1", UserSnapshot.class))
        .thenThrow(new IOException("deserialization error"));

    assertThrows(
        RevertFailedException.class,
        () -> this.handler.revert(this.request),
        "revert() should throw RevertFailedException when deserialization of user-snapshot fails");
  }
}
