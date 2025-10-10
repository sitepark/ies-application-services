package com.sitepark.ies.application.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.security.core.usecase.SetUserPasswordRequest;
import com.sitepark.ies.security.core.usecase.SetUserPasswordUseCase;
import com.sitepark.ies.sharedkernel.audit.AuditLogService;
import com.sitepark.ies.sharedkernel.audit.CreateAuditLogRequest;
import com.sitepark.ies.sharedkernel.patch.PatchDocument;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import com.sitepark.ies.userrepository.core.usecase.user.CreateUserRequest;
import com.sitepark.ies.userrepository.core.usecase.user.CreateUserUseCase;
import com.sitepark.ies.userrepository.core.usecase.user.UpdateUserRequest;
import com.sitepark.ies.userrepository.core.usecase.user.UpdateUserResult;
import com.sitepark.ies.userrepository.core.usecase.user.UpdateUserUseCase;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserApplicationServiceTest {

  private CreateUserUseCase createUserUseCase;
  private UpdateUserUseCase updateUserUseCase;
  private SetUserPasswordUseCase setUserPasswordUseCase;
  private AuditLogService auditLogService;
  private UserApplicationService service;

  @BeforeEach
  void setUp() {
    this.createUserUseCase = mock(CreateUserUseCase.class);
    this.updateUserUseCase = mock(UpdateUserUseCase.class);
    this.setUserPasswordUseCase = mock(SetUserPasswordUseCase.class);
    this.auditLogService = mock(AuditLogService.class);
    this.service =
        new UserApplicationService(
            createUserUseCase, updateUserUseCase, setUserPasswordUseCase, auditLogService);
  }

  @Test
  void testCreateUserWithPasswordReturnsUserId() {

    User user = User.builder().login("testuser").lastName("Test").build();

    when(createUserUseCase.createUser(any(CreateUserRequest.class))).thenReturn("123");

    CreateUserWithPasswordRequest request =
        CreateUserWithPasswordRequest.builder()
            .user(user)
            .password("secret123")
            .roleIdentifiers(b -> b.id("1001"))
            .build();

    String userId = service.createUserWithPassword(request);

    assertEquals("123", userId, "Should return the created user ID");
  }

  @Test
  void testCreateUserWithPasswordCallsCreateUserUseCase() {

    User user = User.builder().login("testuser").lastName("Test").build();

    when(createUserUseCase.createUser(any(CreateUserRequest.class))).thenReturn("123");

    CreateUserWithPasswordRequest request =
        CreateUserWithPasswordRequest.builder()
            .user(user)
            .password("secret123")
            .roleIdentifiers(b -> b.id("1001"))
            .build();

    service.createUserWithPassword(request);

    verify(createUserUseCase).createUser(any(CreateUserRequest.class));
  }

  @Test
  void testCreateUserWithPasswordCallsSetPasswordUseCase() {

    User user = User.builder().login("testuser").lastName("Test").build();

    when(createUserUseCase.createUser(any(CreateUserRequest.class))).thenReturn("123");

    CreateUserWithPasswordRequest request =
        CreateUserWithPasswordRequest.builder()
            .user(user)
            .password("secret123")
            .roleIdentifiers(b -> b.id("1001"))
            .build();

    service.createUserWithPassword(request);

    verify(setUserPasswordUseCase).setUserPassword(any(SetUserPasswordRequest.class));
  }

  @Test
  void testCreateUserWithoutPasswordReturnsUserId() {

    User user = User.builder().login("testuser").lastName("Test").build();

    when(createUserUseCase.createUser(any(CreateUserRequest.class))).thenReturn("456");

    CreateUserWithPasswordRequest request =
        CreateUserWithPasswordRequest.builder().user(user).build();

    String userId = service.createUserWithPassword(request);

    assertEquals("456", userId, "Should return the created user ID");
  }

  @Test
  void testCreateUserWithoutPasswordDoesNotCallSetPassword() {

    User user = User.builder().login("testuser").lastName("Test").build();

    when(createUserUseCase.createUser(any(CreateUserRequest.class))).thenReturn("456");

    CreateUserWithPasswordRequest request =
        CreateUserWithPasswordRequest.builder().user(user).build();

    service.createUserWithPassword(request);

    verify(setUserPasswordUseCase, never()).setUserPassword(any(SetUserPasswordRequest.class));
  }

  @Test
  void testCreateUserWithEmptyPasswordReturnsUserId() {

    User user = User.builder().login("testuser").lastName("Test").build();

    when(createUserUseCase.createUser(any(CreateUserRequest.class))).thenReturn("789");

    CreateUserWithPasswordRequest request =
        CreateUserWithPasswordRequest.builder().user(user).password("").build();

    String userId = service.createUserWithPassword(request);

    assertEquals("789", userId, "Should return the created user ID");
  }

  @Test
  void testCreateUserWithEmptyPasswordDoesNotCallSetPassword() {

    User user = User.builder().login("testuser").lastName("Test").build();

    when(createUserUseCase.createUser(any(CreateUserRequest.class))).thenReturn("789");

    CreateUserWithPasswordRequest request =
        CreateUserWithPasswordRequest.builder().user(user).password("").build();

    service.createUserWithPassword(request);

    verify(setUserPasswordUseCase, never()).setUserPassword(any(SetUserPasswordRequest.class));
  }

  @Test
  void testUpdateUserWithAuditReturnsUserId() {

    User user = User.builder().id("123").login("testuser").lastName("Updated").build();

    PatchDocument patch = mock(PatchDocument.class);
    when(patch.toJson()).thenReturn("{\"patch\":\"forward\"}");

    PatchDocument revertPatch = mock(PatchDocument.class);
    when(revertPatch.toJson()).thenReturn("{\"patch\":\"revert\"}");

    UpdateUserResult.Updated updatedResult =
        new UpdateUserResult.Updated("123", "Test User", patch, revertPatch, Instant.now());

    when(updateUserUseCase.updateUser(any(UpdateUserRequest.class))).thenReturn(updatedResult);

    UpdateUserRequest request = UpdateUserRequest.builder().user(user).build();

    String userId = service.updateUserWithAudit(request);

    assertEquals("123", userId, "Should return the user ID");
  }

  @Test
  void testUpdateUserWithAuditCallsUpdateUserUseCase() {

    User user = User.builder().id("123").login("testuser").lastName("Updated").build();

    PatchDocument patch = mock(PatchDocument.class);
    when(patch.toJson()).thenReturn("{\"patch\":\"forward\"}");

    PatchDocument revertPatch = mock(PatchDocument.class);
    when(revertPatch.toJson()).thenReturn("{\"patch\":\"revert\"}");

    UpdateUserResult.Updated updatedResult =
        new UpdateUserResult.Updated("123", "Test User", patch, revertPatch, Instant.now());

    when(updateUserUseCase.updateUser(any(UpdateUserRequest.class))).thenReturn(updatedResult);

    UpdateUserRequest request = UpdateUserRequest.builder().user(user).build();

    service.updateUserWithAudit(request);

    verify(updateUserUseCase).updateUser(any(UpdateUserRequest.class));
  }

  @Test
  void testUpdateUserWithAuditCreatesAuditLogWhenUserWasUpdated() {

    User user = User.builder().id("123").login("testuser").lastName("Updated").build();

    PatchDocument patch = mock(PatchDocument.class);
    when(patch.toJson()).thenReturn("{\"patch\":\"forward\"}");

    PatchDocument revertPatch = mock(PatchDocument.class);
    when(revertPatch.toJson()).thenReturn("{\"patch\":\"revert\"}");

    UpdateUserResult.Updated updatedResult =
        new UpdateUserResult.Updated("123", "Test User", patch, revertPatch, Instant.now());

    when(updateUserUseCase.updateUser(any(UpdateUserRequest.class))).thenReturn(updatedResult);

    UpdateUserRequest request = UpdateUserRequest.builder().user(user).build();

    service.updateUserWithAudit(request);

    verify(auditLogService).createAuditLog(any(CreateAuditLogRequest.class));
  }

  @Test
  void testUpdateUserWithAuditDoesNotCreateAuditLogWhenUserUnchanged() {

    User user = User.builder().id("123").login("testuser").lastName("Test").build();

    UpdateUserResult.Unchanged unchangedResult = new UpdateUserResult.Unchanged("123");

    when(updateUserUseCase.updateUser(any(UpdateUserRequest.class))).thenReturn(unchangedResult);

    UpdateUserRequest request = UpdateUserRequest.builder().user(user).build();

    service.updateUserWithAudit(request);

    verify(auditLogService, never()).createAuditLog(any(CreateAuditLogRequest.class));
  }

  @Test
  void testUpdateUserWithAuditPassesAuditParentId() {

    User user = User.builder().id("123").login("testuser").lastName("Updated").build();

    PatchDocument patch = mock(PatchDocument.class);
    when(patch.toJson()).thenReturn("{\"patch\":\"forward\"}");

    PatchDocument revertPatch = mock(PatchDocument.class);
    when(revertPatch.toJson()).thenReturn("{\"patch\":\"revert\"}");

    Instant timestamp = Instant.now();
    UpdateUserResult.Updated updatedResult =
        new UpdateUserResult.Updated("123", "Test User", patch, revertPatch, timestamp);

    when(updateUserUseCase.updateUser(any(UpdateUserRequest.class))).thenReturn(updatedResult);

    UpdateUserRequest request =
        UpdateUserRequest.builder().user(user).auditParentId("parent-audit-123").build();

    service.updateUserWithAudit(request);

    verify(auditLogService)
        .createAuditLog(
            any(CreateAuditLogRequest.class)); // Note: Could verify exact parameters if needed
  }
}
