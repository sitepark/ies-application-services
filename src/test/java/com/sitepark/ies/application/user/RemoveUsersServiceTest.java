package com.sitepark.ies.application.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.sharedkernel.audit.AuditLogService;
import com.sitepark.ies.sharedkernel.audit.CreateAuditLogRequest;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import com.sitepark.ies.userrepository.core.usecase.audit.UserSnapshot;
import com.sitepark.ies.userrepository.core.usecase.user.RemoveUserRequest;
import com.sitepark.ies.userrepository.core.usecase.user.RemoveUserResult;
import com.sitepark.ies.userrepository.core.usecase.user.RemoveUserUseCase;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RemoveUsersServiceTest {

  private RemoveUserUseCase removeUserUseCase;
  private AuditLogService auditLogService;
  private Clock clock;
  private RemoveUsersService service;

  @BeforeEach
  void setUp() {
    this.removeUserUseCase = mock();
    this.auditLogService = mock();
    this.clock = mock();
    this.service = new RemoveUsersService(removeUserUseCase, auditLogService, clock);
  }

  @Test
  void testRemoveUsersCallsRemoveUserUseCase() {

    User user = User.builder().id("123").login("testuser").lastName("Test").build();
    UserSnapshot snapshot = new UserSnapshot(user, List.of());
    Instant timestamp = Instant.now();

    RemoveUserResult.Removed removedResult =
        new RemoveUserResult.Removed("123", "Test User", snapshot, timestamp);

    when(removeUserUseCase.removeUser(any(RemoveUserRequest.class))).thenReturn(removedResult);
    when(clock.instant()).thenReturn(timestamp);
    when(clock.getZone()).thenReturn(ZoneId.systemDefault());

    RemoveUsersRequest request = RemoveUsersRequest.builder().identifiers(b -> b.id("123")).build();

    service.removeUsers(request);

    verify(removeUserUseCase).removeUser(any(RemoveUserRequest.class));
  }

  @Test
  void testRemoveUsersCreatesAuditLogWhenUserWasRemoved() throws Exception {

    User user = User.builder().id("123").login("testuser").lastName("Test").build();
    UserSnapshot snapshot = new UserSnapshot(user, List.of());
    Instant timestamp = Instant.now();

    RemoveUserResult.Removed removedResult =
        new RemoveUserResult.Removed("123", "Test User", snapshot, timestamp);

    when(removeUserUseCase.removeUser(any(RemoveUserRequest.class))).thenReturn(removedResult);
    when(clock.instant()).thenReturn(timestamp);
    when(clock.getZone()).thenReturn(ZoneId.systemDefault());
    when(auditLogService.serialize(any())).thenReturn("{\"snapshot\":\"data\"}");

    RemoveUsersRequest request = RemoveUsersRequest.builder().identifiers(b -> b.id("123")).build();

    service.removeUsers(request);

    verify(auditLogService).createAuditLog(any(CreateAuditLogRequest.class));
  }

  @Test
  void testRemoveUsersDoesNotCreateAuditLogWhenUserWasSkipped() {

    RemoveUserResult.Skipped skippedResult =
        new RemoveUserResult.Skipped("1", "Built-in administrator cannot be removed");

    when(removeUserUseCase.removeUser(any(RemoveUserRequest.class))).thenReturn(skippedResult);

    RemoveUsersRequest request = RemoveUsersRequest.builder().identifiers(b -> b.id("1")).build();

    service.removeUsers(request);

    verify(auditLogService, never()).createAuditLog(any(CreateAuditLogRequest.class));
  }

  @Test
  void testRemoveUsersCreatesBatchParentLogForMultipleUsers() throws Exception {

    User user1 = User.builder().id("123").login("user1").lastName("Test1").build();
    User user2 = User.builder().id("456").login("user2").lastName("Test2").build();

    UserSnapshot snapshot1 = new UserSnapshot(user1, List.of());
    UserSnapshot snapshot2 = new UserSnapshot(user2, List.of());

    Instant timestamp = Instant.now();

    RemoveUserResult.Removed removedResult1 =
        new RemoveUserResult.Removed("123", "Test User 1", snapshot1, timestamp);
    RemoveUserResult.Removed removedResult2 =
        new RemoveUserResult.Removed("456", "Test User 2", snapshot2, timestamp);

    when(removeUserUseCase.removeUser(any(RemoveUserRequest.class)))
        .thenReturn(removedResult1, removedResult2);
    when(clock.instant()).thenReturn(timestamp);
    when(clock.getZone()).thenReturn(ZoneId.systemDefault());
    when(auditLogService.serialize(any())).thenReturn("{\"snapshot\":\"data\"}");
    when(auditLogService.createAuditLog(any(CreateAuditLogRequest.class)))
        .thenReturn("batch-parent-id", "audit-1", "audit-2");

    RemoveUsersRequest request =
        RemoveUsersRequest.builder().identifiers(b -> b.id("123").id("456")).build();

    service.removeUsers(request);

    verify(auditLogService, times(3)).createAuditLog(any(CreateAuditLogRequest.class));
  }
}
