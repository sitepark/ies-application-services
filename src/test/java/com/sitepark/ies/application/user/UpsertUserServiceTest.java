package com.sitepark.ies.application.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.label.ReassignLabelsToEntitiesService;
import com.sitepark.ies.application.value.UpsertResult;
import com.sitepark.ies.sharedkernel.patch.PatchDocument;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import com.sitepark.ies.userrepository.core.domain.value.UserSnapshot;
import com.sitepark.ies.userrepository.core.usecase.user.CreateUserResult;
import com.sitepark.ies.userrepository.core.usecase.user.ReassignRolesToUsersResult;
import com.sitepark.ies.userrepository.core.usecase.user.UpdateUserResult;
import com.sitepark.ies.userrepository.core.usecase.user.UpsertUserRequest;
import com.sitepark.ies.userrepository.core.usecase.user.UpsertUserResult;
import com.sitepark.ies.userrepository.core.usecase.user.UpsertUserUseCase;
import com.sitepark.ies.userrepository.core.usecase.user.UserUpdateResult;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UpsertUserServiceTest {

  private UpsertUserUseCase upsertUserUseCase;
  private CreateUserService createUserService;
  private UpdateUserService updateUserService;

  private ReassignLabelsToEntitiesService reassignLabelsToEntitiesService;

  private UpsertUserService service;

  @BeforeEach
  void setUp() {
    this.upsertUserUseCase = mock();
    this.createUserService = mock();
    this.updateUserService = mock();
    this.reassignLabelsToEntitiesService = mock();
    this.service =
        new UpsertUserService(
            upsertUserUseCase,
            createUserService,
            updateUserService,
            reassignLabelsToEntitiesService);
  }

  @Test
  void testUpsertUserCallsUseCase() {

    User user = User.builder().id("123").login("testuser").lastName("Test").build();
    Instant timestamp = Instant.now();
    UpdateUserResult updateUserResult =
        new UpdateUserResult(
            "123", timestamp, UserUpdateResult.unchanged(), ReassignRolesToUsersResult.skipped());
    UpsertUserResult.Updated updatedResult = new UpsertUserResult.Updated("123", updateUserResult);

    when(upsertUserUseCase.upsertUser(any(UpsertUserRequest.class))).thenReturn(updatedResult);

    UpsertUserServiceRequest request =
        UpsertUserServiceRequest.builder()
            .upsertUserRequest(UpsertUserRequest.builder().user(user).build())
            .build();

    service.upsertUser(request);

    verify(upsertUserUseCase).upsertUser(any(UpsertUserRequest.class));
  }

  @Test
  void testUpsertUserReturnsCreatedResultWhenUserCreated() {

    User user = User.builder().id("123").login("testuser").lastName("Test").build();
    UserSnapshot snapshot = new UserSnapshot(user, List.of());
    Instant timestamp = Instant.now();
    CreateUserResult createUserResult = new CreateUserResult("123", snapshot, null, timestamp);
    UpsertUserResult.Created createdResult = new UpsertUserResult.Created("123", createUserResult);

    when(upsertUserUseCase.upsertUser(any(UpsertUserRequest.class))).thenReturn(createdResult);

    UpsertUserServiceRequest request =
        UpsertUserServiceRequest.builder()
            .upsertUserRequest(UpsertUserRequest.builder().user(user).build())
            .build();

    UpsertResult result = service.upsertUser(request);

    assertInstanceOf(
        UpsertResult.Created.class,
        result,
        "upsertUser() should return Created when user was created");
  }

  @Test
  void testUpsertUserCallsCreateAuditLogsWhenCreated() {

    User user = User.builder().id("123").login("testuser").lastName("Test").build();
    UserSnapshot snapshot = new UserSnapshot(user, List.of());
    Instant timestamp = Instant.now();
    CreateUserResult createUserResult = new CreateUserResult("123", snapshot, null, timestamp);
    UpsertUserResult.Created createdResult = new UpsertUserResult.Created("123", createUserResult);

    when(upsertUserUseCase.upsertUser(any(UpsertUserRequest.class))).thenReturn(createdResult);

    UpsertUserServiceRequest request =
        UpsertUserServiceRequest.builder()
            .upsertUserRequest(UpsertUserRequest.builder().user(user).build())
            .build();

    service.upsertUser(request);

    verify(createUserService).createAuditLogs(any(), any());
  }

  @Test
  void testUpsertUserReturnsUpdatedTrueWhenUserChanged() {

    User user = User.builder().id("123").login("testuser").lastName("Test").build();
    Instant timestamp = Instant.now();
    PatchDocument patchDoc = mock();
    UpdateUserResult updateUserResult =
        new UpdateUserResult(
            "123",
            timestamp,
            UserUpdateResult.updated("Test User", patchDoc, patchDoc),
            ReassignRolesToUsersResult.skipped());
    UpsertUserResult.Updated updatedResult = new UpsertUserResult.Updated("123", updateUserResult);

    when(upsertUserUseCase.upsertUser(any(UpsertUserRequest.class))).thenReturn(updatedResult);

    UpsertUserServiceRequest request =
        UpsertUserServiceRequest.builder()
            .upsertUserRequest(UpsertUserRequest.builder().user(user).build())
            .build();

    UpsertResult result = service.upsertUser(request);

    assertEquals(
        UpsertResult.updated(true),
        result,
        "upsertUser() should return Updated(true) when changes were made");
  }

  @Test
  void testUpsertUserCallsUpdateAuditLogsWhenUserChanged() {

    User user = User.builder().id("123").login("testuser").lastName("Test").build();
    Instant timestamp = Instant.now();
    PatchDocument patchDoc = mock();
    UpdateUserResult updateUserResult =
        new UpdateUserResult(
            "123",
            timestamp,
            UserUpdateResult.updated("Test User", patchDoc, patchDoc),
            ReassignRolesToUsersResult.skipped());
    UpsertUserResult.Updated updatedResult = new UpsertUserResult.Updated("123", updateUserResult);

    when(upsertUserUseCase.upsertUser(any(UpsertUserRequest.class))).thenReturn(updatedResult);

    UpsertUserServiceRequest request =
        UpsertUserServiceRequest.builder()
            .upsertUserRequest(UpsertUserRequest.builder().user(user).build())
            .build();

    service.upsertUser(request);

    verify(updateUserService).createAuditLogs(any(), any());
  }

  @Test
  void testUpsertUserReturnsUpdatedFalseWhenNoChanges() {

    User user = User.builder().id("123").login("testuser").lastName("Test").build();
    Instant timestamp = Instant.now();
    UpdateUserResult updateUserResult =
        new UpdateUserResult(
            "123", timestamp, UserUpdateResult.unchanged(), ReassignRolesToUsersResult.skipped());
    UpsertUserResult.Updated updatedResult = new UpsertUserResult.Updated("123", updateUserResult);

    when(upsertUserUseCase.upsertUser(any(UpsertUserRequest.class))).thenReturn(updatedResult);

    UpsertUserServiceRequest request =
        UpsertUserServiceRequest.builder()
            .upsertUserRequest(UpsertUserRequest.builder().user(user).build())
            .build();

    UpsertResult result = service.upsertUser(request);

    assertEquals(
        UpsertResult.updated(false),
        result,
        "upsertUser() should return Updated(false) when no changes were made");
  }

  @Test
  void testUpsertUserDoesNotCallUpdateAuditLogsWhenNoChanges() {

    User user = User.builder().id("123").login("testuser").lastName("Test").build();
    Instant timestamp = Instant.now();
    UpdateUserResult updateUserResult =
        new UpdateUserResult(
            "123", timestamp, UserUpdateResult.unchanged(), ReassignRolesToUsersResult.skipped());
    UpsertUserResult.Updated updatedResult = new UpsertUserResult.Updated("123", updateUserResult);

    when(upsertUserUseCase.upsertUser(any(UpsertUserRequest.class))).thenReturn(updatedResult);

    UpsertUserServiceRequest request =
        UpsertUserServiceRequest.builder()
            .upsertUserRequest(UpsertUserRequest.builder().user(user).build())
            .build();

    service.upsertUser(request);

    verify(updateUserService, never()).createAuditLogs(any(), any());
  }

  @Test
  void testUpsertUserReassignsLabelsWhenLabelIdentifiersProvided() {

    User user = User.builder().id("123").login("testuser").lastName("Test").build();
    Instant timestamp = Instant.now();
    UpdateUserResult updateUserResult =
        new UpdateUserResult(
            "123", timestamp, UserUpdateResult.unchanged(), ReassignRolesToUsersResult.skipped());
    UpsertUserResult.Updated updatedResult = new UpsertUserResult.Updated("123", updateUserResult);

    when(upsertUserUseCase.upsertUser(any(UpsertUserRequest.class))).thenReturn(updatedResult);

    UpsertUserServiceRequest request =
        UpsertUserServiceRequest.builder()
            .upsertUserRequest(UpsertUserRequest.builder().user(user).build())
            .labelIdentifiers(b -> b.id("501"))
            .build();

    service.upsertUser(request);

    verify(reassignLabelsToEntitiesService).reassignEntitiesFromLabels(any());
  }

  @Test
  void testUpsertUserDoesNotReassignLabelsWhenNoLabelIdentifiers() {

    User user = User.builder().id("123").login("testuser").lastName("Test").build();
    Instant timestamp = Instant.now();
    UpdateUserResult updateUserResult =
        new UpdateUserResult(
            "123", timestamp, UserUpdateResult.unchanged(), ReassignRolesToUsersResult.skipped());
    UpsertUserResult.Updated updatedResult = new UpsertUserResult.Updated("123", updateUserResult);

    when(upsertUserUseCase.upsertUser(any(UpsertUserRequest.class))).thenReturn(updatedResult);

    UpsertUserServiceRequest request =
        UpsertUserServiceRequest.builder()
            .upsertUserRequest(UpsertUserRequest.builder().user(user).build())
            .build();

    service.upsertUser(request);

    verify(reassignLabelsToEntitiesService, never()).reassignEntitiesFromLabels(any());
  }
}
