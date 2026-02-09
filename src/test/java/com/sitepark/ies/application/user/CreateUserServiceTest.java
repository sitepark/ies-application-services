package com.sitepark.ies.application.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.sharedkernel.audit.AuditLogService;
import com.sitepark.ies.sharedkernel.audit.CreateAuditLogRequest;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import com.sitepark.ies.userrepository.core.domain.value.UserRoleAssignment;
import com.sitepark.ies.userrepository.core.port.UserRepository;
import com.sitepark.ies.userrepository.core.usecase.audit.UserSnapshot;
import com.sitepark.ies.userrepository.core.usecase.user.AssignRolesToUsersResult;
import com.sitepark.ies.userrepository.core.usecase.user.CreateUserResult;
import com.sitepark.ies.userrepository.core.usecase.user.CreateUserUseCase;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreateUserServiceTest {

  private CreateUserUseCase createUserUseCase;
  private UserRepository userRepository;
  private AuditLogService auditLogService;
  private CreateUserService service;

  @BeforeEach
  void setUp() {
    this.createUserUseCase = mock();
    this.userRepository = mock();
    this.auditLogService = mock();
    this.service = new CreateUserService(createUserUseCase, userRepository, auditLogService);
  }

  @Test
  void testCreateUserReturnsUserId() {

    User user = User.builder().id("123").login("testuser").lastName("Test").build();
    UserSnapshot snapshot = new UserSnapshot(user, List.of());
    Instant timestamp = Instant.now();

    CreateUserResult result = new CreateUserResult("123", snapshot, null, timestamp);

    when(createUserUseCase.createUser(
            any(com.sitepark.ies.userrepository.core.usecase.user.CreateUserRequest.class)))
        .thenReturn(result);

    CreateUserRequest request = CreateUserRequest.builder().user(user).build();

    String userId = service.createUser(request);

    assertEquals("123", userId, "Should return the created user ID");
  }

  @Test
  void testCreateUserCallsUseCase() {

    User user = User.builder().id("123").login("testuser").lastName("Test").build();
    UserSnapshot snapshot = new UserSnapshot(user, List.of());
    Instant timestamp = Instant.now();

    CreateUserResult result = new CreateUserResult("123", snapshot, null, timestamp);

    when(createUserUseCase.createUser(
            any(com.sitepark.ies.userrepository.core.usecase.user.CreateUserRequest.class)))
        .thenReturn(result);

    CreateUserRequest request = CreateUserRequest.builder().user(user).build();

    service.createUser(request);

    verify(createUserUseCase)
        .createUser(any(com.sitepark.ies.userrepository.core.usecase.user.CreateUserRequest.class));
  }

  @Test
  void testCreateUserCreatesAuditLogForUserCreation() throws Exception {

    User user = User.builder().id("123").login("testuser").lastName("Test").build();
    UserSnapshot snapshot = new UserSnapshot(user, List.of());
    Instant timestamp = Instant.now();

    CreateUserResult result = new CreateUserResult("123", snapshot, null, timestamp);

    when(createUserUseCase.createUser(
            any(com.sitepark.ies.userrepository.core.usecase.user.CreateUserRequest.class)))
        .thenReturn(result);
    when(auditLogService.serialize(any(UserSnapshot.class))).thenReturn("{\"snapshot\":\"data\"}");

    CreateUserRequest request = CreateUserRequest.builder().user(user).build();

    service.createUser(request);

    verify(auditLogService).createAuditLog(any(CreateAuditLogRequest.class));
  }

  @Test
  void testCreateUserCreatesAuditLogsForRoleAssignments() throws Exception {

    User user = User.builder().id("123").login("testuser").lastName("Test").build();
    UserSnapshot snapshot = new UserSnapshot(user, List.of("101", "102"));

    UserRoleAssignment assignments =
        UserRoleAssignment.builder().assignments("123", List.of("101", "102")).build();
    Instant timestamp = Instant.now();
    AssignRolesToUsersResult.Assigned roleResult =
        new AssignRolesToUsersResult.Assigned(assignments, timestamp);

    CreateUserResult result = new CreateUserResult("123", snapshot, roleResult, timestamp);

    when(createUserUseCase.createUser(
            any(com.sitepark.ies.userrepository.core.usecase.user.CreateUserRequest.class)))
        .thenReturn(result);
    when(userRepository.get("123")).thenReturn(Optional.of(user));
    when(auditLogService.serialize(any(UserSnapshot.class))).thenReturn("{\"snapshot\":\"data\"}");
    when(auditLogService.serialize(any(List.class))).thenReturn("[\"101\",\"102\"]");

    CreateUserRequest request =
        CreateUserRequest.builder().user(user).roleIdentifiers(b -> b.id("101").id("102")).build();

    service.createUser(request);

    verify(auditLogService, times(2)).createAuditLog(any(CreateAuditLogRequest.class));
  }

  @Test
  void testCreateUserDoesNotCreateRoleAuditLogWhenNoRoles() throws Exception {

    User user = User.builder().id("123").login("testuser").lastName("Test").build();
    UserSnapshot snapshot = new UserSnapshot(user, List.of());
    Instant timestamp = Instant.now();

    CreateUserResult result = new CreateUserResult("123", snapshot, null, timestamp);

    when(createUserUseCase.createUser(
            any(com.sitepark.ies.userrepository.core.usecase.user.CreateUserRequest.class)))
        .thenReturn(result);
    when(auditLogService.serialize(any(UserSnapshot.class))).thenReturn("{\"snapshot\":\"data\"}");

    CreateUserRequest request = CreateUserRequest.builder().user(user).build();

    service.createUser(request);

    verify(auditLogService, times(1)).createAuditLog(any(CreateAuditLogRequest.class));
  }
}
