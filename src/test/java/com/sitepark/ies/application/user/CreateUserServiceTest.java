package com.sitepark.ies.application.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.label.ReassignLabelsToEntitiesService;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import com.sitepark.ies.userrepository.core.domain.value.UserRoleAssignment;
import com.sitepark.ies.userrepository.core.domain.value.UserSnapshot;
import com.sitepark.ies.userrepository.core.usecase.user.AssignRolesToUsersResult;
import com.sitepark.ies.userrepository.core.usecase.user.CreateUserResult;
import com.sitepark.ies.userrepository.core.usecase.user.CreateUserUseCase;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreateUserServiceTest {

  private CreateUserUseCase createUserUseCase;

  @SuppressWarnings("PMD.SingularField")
  private ReassignLabelsToEntitiesService reassignLabelsToEntitiesService;

  @SuppressWarnings("PMD.SingularField")
  private ApplicationAuditLogServiceFactory auditLogServiceFactory;

  private ApplicationAuditLogService auditLogService;
  private CreateUserService service;

  @BeforeEach
  void setUp() {
    this.createUserUseCase = mock();
    this.reassignLabelsToEntitiesService = mock();
    this.auditLogServiceFactory = mock();
    this.auditLogService = mock();
    this.service =
        new CreateUserService(
            createUserUseCase, reassignLabelsToEntitiesService, auditLogServiceFactory);

    when(auditLogServiceFactory.create(any(), any())).thenReturn(auditLogService);
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

    CreateUserServiceRequest request =
        CreateUserServiceRequest.builder()
            .createUserRequest(
                com.sitepark.ies.userrepository.core.usecase.user.CreateUserRequest.builder()
                    .user(user)
                    .build())
            .build();

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

    CreateUserServiceRequest request =
        CreateUserServiceRequest.builder()
            .createUserRequest(
                com.sitepark.ies.userrepository.core.usecase.user.CreateUserRequest.builder()
                    .user(user)
                    .build())
            .build();

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

    CreateUserServiceRequest request =
        CreateUserServiceRequest.builder()
            .createUserRequest(
                com.sitepark.ies.userrepository.core.usecase.user.CreateUserRequest.builder()
                    .user(user)
                    .build())
            .build();

    service.createUser(request);

    verify(auditLogService).createLog(any(), any(), any(), any(), any());
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

    CreateUserServiceRequest request =
        CreateUserServiceRequest.builder()
            .createUserRequest(
                com.sitepark.ies.userrepository.core.usecase.user.CreateUserRequest.builder()
                    .user(user)
                    .roleIdentifiers(b -> b.id("101").id("102"))
                    .build())
            .build();

    service.createUser(request);

    verify(auditLogService, times(2)).createLog(any(), any(), any(), any(), any());
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

    CreateUserServiceRequest request =
        CreateUserServiceRequest.builder()
            .createUserRequest(
                com.sitepark.ies.userrepository.core.usecase.user.CreateUserRequest.builder()
                    .user(user)
                    .build())
            .build();

    service.createUser(request);

    verify(auditLogService, times(1)).createLog(any(), any(), any(), any(), any());
  }
}
