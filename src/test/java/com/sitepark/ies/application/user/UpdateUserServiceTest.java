package com.sitepark.ies.application.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.MultiEntityNameResolver;
import com.sitepark.ies.application.label.ReassignLabelsToEntitiesService;
import com.sitepark.ies.sharedkernel.patch.PatchDocument;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import com.sitepark.ies.userrepository.core.domain.value.UserRoleAssignment;
import com.sitepark.ies.userrepository.core.usecase.user.ReassignRolesToUsersResult;
import com.sitepark.ies.userrepository.core.usecase.user.UpdateUserResult;
import com.sitepark.ies.userrepository.core.usecase.user.UpdateUserUseCase;
import com.sitepark.ies.userrepository.core.usecase.user.UserUpdateResult;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UpdateUserServiceTest {

  private UpdateUserUseCase updateUserUseCase;

  private ReassignLabelsToEntitiesService reassignLabelsToEntitiesService;

  private MultiEntityNameResolver multiEntityNameResolver;

  @SuppressWarnings("PMD.SingularField")
  private ApplicationAuditLogServiceFactory auditLogServiceFactory;

  private ApplicationAuditLogService auditLogService;

  private UpdateUserService service;

  @BeforeEach
  void setUp() {
    this.updateUserUseCase = mock();
    this.reassignLabelsToEntitiesService = mock();
    this.multiEntityNameResolver = mock();
    this.auditLogServiceFactory = mock();
    this.auditLogService = mock();
    this.service =
        new UpdateUserService(
            updateUserUseCase,
            reassignLabelsToEntitiesService,
            multiEntityNameResolver,
            auditLogServiceFactory);
    when(auditLogServiceFactory.create(any(), any())).thenReturn(auditLogService);
  }

  @Test
  void testUpdateUserReturnsUserId() {

    User user = User.builder().id("123").login("testuser").lastName("Updated").build();

    PatchDocument patch = mock(PatchDocument.class);
    when(patch.toJson()).thenReturn("{\"patch\":\"forward\"}");

    PatchDocument revertPatch = mock(PatchDocument.class);
    when(revertPatch.toJson()).thenReturn("{\"patch\":\"revert\"}");

    Instant timestamp = Instant.now();
    UserUpdateResult userUpdateResult = UserUpdateResult.updated("Test User", patch, revertPatch);
    UpdateUserResult result =
        new UpdateUserResult(
            "123", timestamp, userUpdateResult, ReassignRolesToUsersResult.skipped());

    when(updateUserUseCase.updateUser(
            any(com.sitepark.ies.userrepository.core.usecase.user.UpdateUserRequest.class)))
        .thenReturn(result);

    UpdateUserServiceRequest request =
        UpdateUserServiceRequest.builder()
            .updateUserRequest(
                com.sitepark.ies.userrepository.core.usecase.user.UpdateUserRequest.builder()
                    .user(user)
                    .build())
            .build();

    String userId = service.updateUser(request);

    assertEquals("123", userId, "Should return the user ID");
  }

  @Test
  void testUpdateUserCallsUpdateUserUseCase() {

    User user = User.builder().id("123").login("testuser").lastName("Updated").build();

    PatchDocument patch = mock(PatchDocument.class);
    when(patch.toJson()).thenReturn("{\"patch\":\"forward\"}");

    PatchDocument revertPatch = mock(PatchDocument.class);
    when(revertPatch.toJson()).thenReturn("{\"patch\":\"revert\"}");

    Instant timestamp = Instant.now();
    UserUpdateResult userUpdateResult = UserUpdateResult.updated("Test User", patch, revertPatch);
    UpdateUserResult result =
        new UpdateUserResult(
            "123", timestamp, userUpdateResult, ReassignRolesToUsersResult.skipped());

    when(updateUserUseCase.updateUser(
            any(com.sitepark.ies.userrepository.core.usecase.user.UpdateUserRequest.class)))
        .thenReturn(result);

    UpdateUserServiceRequest request =
        UpdateUserServiceRequest.builder()
            .updateUserRequest(
                com.sitepark.ies.userrepository.core.usecase.user.UpdateUserRequest.builder()
                    .user(user)
                    .build())
            .build();

    service.updateUser(request);

    verify(updateUserUseCase)
        .updateUser(any(com.sitepark.ies.userrepository.core.usecase.user.UpdateUserRequest.class));
  }

  @Test
  void testUpdateUserCreatesAuditLogWhenUserWasUpdated() {

    User user = User.builder().id("123").login("testuser").lastName("Updated").build();

    PatchDocument patch = mock(PatchDocument.class);
    when(patch.toJson()).thenReturn("{\"patch\":\"forward\"}");

    PatchDocument revertPatch = mock(PatchDocument.class);
    when(revertPatch.toJson()).thenReturn("{\"patch\":\"revert\"}");

    Instant timestamp = Instant.now();
    UserUpdateResult userUpdateResult = UserUpdateResult.updated("Test User", patch, revertPatch);
    UpdateUserResult result =
        new UpdateUserResult(
            "123", timestamp, userUpdateResult, ReassignRolesToUsersResult.skipped());

    when(updateUserUseCase.updateUser(
            any(com.sitepark.ies.userrepository.core.usecase.user.UpdateUserRequest.class)))
        .thenReturn(result);

    UpdateUserServiceRequest request =
        UpdateUserServiceRequest.builder()
            .updateUserRequest(
                com.sitepark.ies.userrepository.core.usecase.user.UpdateUserRequest.builder()
                    .user(user)
                    .build())
            .build();

    service.updateUser(request);

    verify(auditLogService).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testUpdateUserDoesNotCreateAuditLogWhenUserUnchanged() {

    User user = User.builder().id("123").login("testuser").lastName("Test").build();

    Instant timestamp = Instant.now();
    UserUpdateResult userUpdateResult = UserUpdateResult.unchanged();
    UpdateUserResult result =
        new UpdateUserResult(
            "123", timestamp, userUpdateResult, ReassignRolesToUsersResult.skipped());

    when(updateUserUseCase.updateUser(
            any(com.sitepark.ies.userrepository.core.usecase.user.UpdateUserRequest.class)))
        .thenReturn(result);

    UpdateUserServiceRequest request =
        UpdateUserServiceRequest.builder()
            .updateUserRequest(
                com.sitepark.ies.userrepository.core.usecase.user.UpdateUserRequest.builder()
                    .user(user)
                    .build())
            .build();

    service.updateUser(request);

    verify(auditLogService, never()).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testUpdateUserPassesAuditParentId() {

    User user = User.builder().id("123").login("testuser").lastName("Updated").build();

    PatchDocument patch = mock(PatchDocument.class);
    when(patch.toJson()).thenReturn("{\"patch\":\"forward\"}");

    PatchDocument revertPatch = mock(PatchDocument.class);
    when(revertPatch.toJson()).thenReturn("{\"patch\":\"revert\"}");

    Instant timestamp = Instant.now();
    UserUpdateResult userUpdateResult = UserUpdateResult.updated("Test User", patch, revertPatch);
    UpdateUserResult result =
        new UpdateUserResult(
            "123", timestamp, userUpdateResult, ReassignRolesToUsersResult.skipped());

    when(updateUserUseCase.updateUser(
            any(com.sitepark.ies.userrepository.core.usecase.user.UpdateUserRequest.class)))
        .thenReturn(result);

    UpdateUserServiceRequest request =
        UpdateUserServiceRequest.builder()
            .updateUserRequest(
                com.sitepark.ies.userrepository.core.usecase.user.UpdateUserRequest.builder()
                    .user(user)
                    .build())
            .auditParentId("parent-audit-123")
            .build();

    service.updateUser(request);

    verify(auditLogService).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testUpdateUserReassignsLabelsWhenLabelIdentifiersProvided() {

    User user = User.builder().id("123").login("testuser").lastName("Test").build();

    Instant timestamp = Instant.now();
    UpdateUserResult result =
        new UpdateUserResult(
            "123", timestamp, UserUpdateResult.unchanged(), ReassignRolesToUsersResult.skipped());

    when(updateUserUseCase.updateUser(
            any(com.sitepark.ies.userrepository.core.usecase.user.UpdateUserRequest.class)))
        .thenReturn(result);

    UpdateUserServiceRequest request =
        UpdateUserServiceRequest.builder()
            .updateUserRequest(
                com.sitepark.ies.userrepository.core.usecase.user.UpdateUserRequest.builder()
                    .user(user)
                    .build())
            .labelIdentifiers(b -> b.id("501"))
            .build();

    service.updateUser(request);

    verify(reassignLabelsToEntitiesService).reassignEntitiesFromLabels(any());
  }

  @Test
  void testUpdateUserDoesNotReassignLabelsWhenNoLabelIdentifiers() {

    User user = User.builder().id("123").login("testuser").lastName("Test").build();

    Instant timestamp = Instant.now();
    UpdateUserResult result =
        new UpdateUserResult(
            "123", timestamp, UserUpdateResult.unchanged(), ReassignRolesToUsersResult.skipped());

    when(updateUserUseCase.updateUser(
            any(com.sitepark.ies.userrepository.core.usecase.user.UpdateUserRequest.class)))
        .thenReturn(result);

    UpdateUserServiceRequest request =
        UpdateUserServiceRequest.builder()
            .updateUserRequest(
                com.sitepark.ies.userrepository.core.usecase.user.UpdateUserRequest.builder()
                    .user(user)
                    .build())
            .build();

    service.updateUser(request);

    verify(reassignLabelsToEntitiesService, never()).reassignEntitiesFromLabels(any());
  }

  @Test
  void testUpdateUserCreatesAuditLogForRoleAssignment() {

    User user = User.builder().id("123").login("testuser").lastName("Test").build();

    Instant timestamp = Instant.now();
    UserRoleAssignment assignments =
        UserRoleAssignment.builder().assignments("123", "role-1").build();
    UserRoleAssignment emptyUnassignments = UserRoleAssignment.builder().build();
    ReassignRolesToUsersResult roleResult =
        ReassignRolesToUsersResult.reassigned(assignments, emptyUnassignments, timestamp);
    UpdateUserResult result =
        new UpdateUserResult("123", timestamp, UserUpdateResult.unchanged(), roleResult);

    when(updateUserUseCase.updateUser(
            any(com.sitepark.ies.userrepository.core.usecase.user.UpdateUserRequest.class)))
        .thenReturn(result);
    when(multiEntityNameResolver.resolveDisplayUserName(any())).thenReturn("Test User");

    UpdateUserServiceRequest request =
        UpdateUserServiceRequest.builder()
            .updateUserRequest(
                com.sitepark.ies.userrepository.core.usecase.user.UpdateUserRequest.builder()
                    .user(user)
                    .build())
            .build();

    service.updateUser(request);

    verify(auditLogService).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testUpdateUserCreatesAuditLogForRoleUnassignment() {

    User user = User.builder().id("123").login("testuser").lastName("Test").build();

    Instant timestamp = Instant.now();
    UserRoleAssignment emptyAssignments = UserRoleAssignment.builder().build();
    UserRoleAssignment unassignments =
        UserRoleAssignment.builder().assignments("123", "role-1").build();
    ReassignRolesToUsersResult roleResult =
        ReassignRolesToUsersResult.reassigned(emptyAssignments, unassignments, timestamp);
    UpdateUserResult result =
        new UpdateUserResult("123", timestamp, UserUpdateResult.unchanged(), roleResult);

    when(updateUserUseCase.updateUser(
            any(com.sitepark.ies.userrepository.core.usecase.user.UpdateUserRequest.class)))
        .thenReturn(result);
    when(multiEntityNameResolver.resolveDisplayUserName(any())).thenReturn("Test User");

    UpdateUserServiceRequest request =
        UpdateUserServiceRequest.builder()
            .updateUserRequest(
                com.sitepark.ies.userrepository.core.usecase.user.UpdateUserRequest.builder()
                    .user(user)
                    .build())
            .build();

    service.updateUser(request);

    verify(auditLogService).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testUpdateUserCreatesBatchAuditLogWhenMultipleRoleChanges() {

    User user = User.builder().id("123").login("testuser").lastName("Test").build();

    Instant timestamp = Instant.now();
    UserRoleAssignment assignments =
        UserRoleAssignment.builder().assignments("123", "role-1").build();
    UserRoleAssignment unassignments =
        UserRoleAssignment.builder().assignments("123", "role-2").build();
    ReassignRolesToUsersResult roleResult =
        ReassignRolesToUsersResult.reassigned(assignments, unassignments, timestamp);
    UpdateUserResult result =
        new UpdateUserResult("123", timestamp, UserUpdateResult.unchanged(), roleResult);

    when(updateUserUseCase.updateUser(
            any(com.sitepark.ies.userrepository.core.usecase.user.UpdateUserRequest.class)))
        .thenReturn(result);
    when(multiEntityNameResolver.resolveDisplayUserName(any())).thenReturn("Test User");
    when(auditLogService.createBatchLog(any(), any())).thenReturn("batch-id");

    UpdateUserServiceRequest request =
        UpdateUserServiceRequest.builder()
            .updateUserRequest(
                com.sitepark.ies.userrepository.core.usecase.user.UpdateUserRequest.builder()
                    .user(user)
                    .build())
            .build();

    service.updateUser(request);

    verify(auditLogService).createBatchLog(any(), any());
  }

  @Test
  void testUpdateUserResolvesDisplayNameWhenNoUserChangesButRolesReassigned() {

    User user = User.builder().id("123").login("testuser").lastName("Test").build();

    Instant timestamp = Instant.now();
    UserRoleAssignment assignments =
        UserRoleAssignment.builder().assignments("123", "role-1").build();
    UserRoleAssignment emptyUnassignments = UserRoleAssignment.builder().build();
    ReassignRolesToUsersResult roleResult =
        ReassignRolesToUsersResult.reassigned(assignments, emptyUnassignments, timestamp);
    UpdateUserResult result =
        new UpdateUserResult("123", timestamp, UserUpdateResult.unchanged(), roleResult);

    when(updateUserUseCase.updateUser(
            any(com.sitepark.ies.userrepository.core.usecase.user.UpdateUserRequest.class)))
        .thenReturn(result);
    when(multiEntityNameResolver.resolveDisplayUserName(any())).thenReturn("Test User");

    UpdateUserServiceRequest request =
        UpdateUserServiceRequest.builder()
            .updateUserRequest(
                com.sitepark.ies.userrepository.core.usecase.user.UpdateUserRequest.builder()
                    .user(user)
                    .build())
            .build();

    service.updateUser(request);

    verify(multiEntityNameResolver).resolveDisplayUserName(any());
  }

  @Test
  void testUpdateUserUsesDisplayNameFromUpdateWhenUserChangedAndRolesReassigned() {

    User user = User.builder().id("123").login("testuser").lastName("Test").build();

    PatchDocument patch = mock(PatchDocument.class);
    when(patch.toJson()).thenReturn("{\"patch\":\"forward\"}");

    PatchDocument revertPatch = mock(PatchDocument.class);
    when(revertPatch.toJson()).thenReturn("{\"patch\":\"revert\"}");

    Instant timestamp = Instant.now();
    UserUpdateResult userUpdateResult = UserUpdateResult.updated("Test User", patch, revertPatch);
    UserRoleAssignment assignments =
        UserRoleAssignment.builder().assignments("123", "role-1").build();
    UserRoleAssignment emptyUnassignments = UserRoleAssignment.builder().build();
    ReassignRolesToUsersResult roleResult =
        ReassignRolesToUsersResult.reassigned(assignments, emptyUnassignments, timestamp);
    UpdateUserResult result = new UpdateUserResult("123", timestamp, userUpdateResult, roleResult);

    when(updateUserUseCase.updateUser(
            any(com.sitepark.ies.userrepository.core.usecase.user.UpdateUserRequest.class)))
        .thenReturn(result);

    UpdateUserServiceRequest request =
        UpdateUserServiceRequest.builder()
            .updateUserRequest(
                com.sitepark.ies.userrepository.core.usecase.user.UpdateUserRequest.builder()
                    .user(user)
                    .build())
            .build();

    service.updateUser(request);

    verify(multiEntityNameResolver, never()).resolveDisplayUserName(any());
  }
}
