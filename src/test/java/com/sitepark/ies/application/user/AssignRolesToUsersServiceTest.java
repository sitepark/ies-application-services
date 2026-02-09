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
import com.sitepark.ies.userrepository.core.domain.value.UserRoleAssignment;
import com.sitepark.ies.userrepository.core.port.UserRepository;
import com.sitepark.ies.userrepository.core.usecase.user.AssignRolesToUsersResult;
import com.sitepark.ies.userrepository.core.usecase.user.AssignRolesToUsersUseCase;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AssignRolesToUsersServiceTest {

  private AssignRolesToUsersUseCase assignRolesToUsersUseCase;
  private UserRepository userRepository;
  private AuditLogService auditLogService;
  private AssignRolesToUsersService service;

  @BeforeEach
  void setUp() {
    this.assignRolesToUsersUseCase = mock();
    this.userRepository = mock();
    this.auditLogService = mock();
    this.service =
        new AssignRolesToUsersService(assignRolesToUsersUseCase, userRepository, auditLogService);
  }

  @Test
  void testAssignRolesToUsersCallsUseCase() {

    UserRoleAssignment assignments =
        UserRoleAssignment.builder().assignments("123", List.of("101", "102")).build();

    Instant timestamp = Instant.now();
    AssignRolesToUsersResult.Assigned assignedResult =
        new AssignRolesToUsersResult.Assigned(assignments, timestamp);

    when(assignRolesToUsersUseCase.assignRolesToUsers(
            any(com.sitepark.ies.userrepository.core.usecase.user.AssignRolesToUsersRequest.class)))
        .thenReturn(assignedResult);

    AssignRolesToUsersRequest request =
        AssignRolesToUsersRequest.builder()
            .userIdentifiers(b -> b.id("123"))
            .roleIdentifiers(b -> b.id("101").id("102"))
            .build();

    service.assignRolesToUsers(request);

    verify(assignRolesToUsersUseCase)
        .assignRolesToUsers(
            any(com.sitepark.ies.userrepository.core.usecase.user.AssignRolesToUsersRequest.class));
  }

  @Test
  void testAssignRolesToUsersCreatesAuditLogWhenAssigned() throws Exception {

    User user = User.builder().id("123").login("testuser").lastName("Test").build();
    UserRoleAssignment assignments =
        UserRoleAssignment.builder().assignments("123", List.of("101", "102")).build();

    Instant timestamp = Instant.now();
    AssignRolesToUsersResult.Assigned assignedResult =
        new AssignRolesToUsersResult.Assigned(assignments, timestamp);

    when(assignRolesToUsersUseCase.assignRolesToUsers(
            any(com.sitepark.ies.userrepository.core.usecase.user.AssignRolesToUsersRequest.class)))
        .thenReturn(assignedResult);
    when(userRepository.get("123")).thenReturn(Optional.of(user));
    when(auditLogService.serialize(any())).thenReturn("[\"101\",\"102\"]");

    AssignRolesToUsersRequest request =
        AssignRolesToUsersRequest.builder()
            .userIdentifiers(b -> b.id("123"))
            .roleIdentifiers(b -> b.id("101").id("102"))
            .build();

    service.assignRolesToUsers(request);

    verify(auditLogService).createAuditLog(any(CreateAuditLogRequest.class));
  }

  @Test
  void testAssignRolesToUsersDoesNotCreateAuditLogWhenSkipped() {

    UserRoleAssignment emptyAssignments = UserRoleAssignment.builder().build();
    AssignRolesToUsersResult.Skipped skippedResult =
        new AssignRolesToUsersResult.Skipped(emptyAssignments);

    when(assignRolesToUsersUseCase.assignRolesToUsers(
            any(com.sitepark.ies.userrepository.core.usecase.user.AssignRolesToUsersRequest.class)))
        .thenReturn(skippedResult);

    AssignRolesToUsersRequest request =
        AssignRolesToUsersRequest.builder()
            .userIdentifiers(b -> b.id("123"))
            .roleIdentifiers(b -> b.id("101"))
            .build();

    service.assignRolesToUsers(request);

    verify(auditLogService, never()).createAuditLog(any(CreateAuditLogRequest.class));
  }

  @Test
  void testAssignRolesToUsersCreatesBatchParentLogForMultipleUsers() throws Exception {

    User user1 = User.builder().id("123").login("user1").lastName("Test1").build();
    User user2 = User.builder().id("456").login("user2").lastName("Test2").build();

    UserRoleAssignment assignments =
        UserRoleAssignment.builder()
            .assignments("123", List.of("101"))
            .assignments("456", List.of("101"))
            .build();

    Instant timestamp = Instant.now();
    AssignRolesToUsersResult.Assigned assignedResult =
        new AssignRolesToUsersResult.Assigned(assignments, timestamp);

    when(assignRolesToUsersUseCase.assignRolesToUsers(
            any(com.sitepark.ies.userrepository.core.usecase.user.AssignRolesToUsersRequest.class)))
        .thenReturn(assignedResult);
    when(userRepository.get("123")).thenReturn(Optional.of(user1));
    when(userRepository.get("456")).thenReturn(Optional.of(user2));
    when(auditLogService.serialize(any())).thenReturn("[\"101\"]");
    when(auditLogService.createAuditLog(any(CreateAuditLogRequest.class)))
        .thenReturn("batch-parent-id", "audit-1", "audit-2");

    AssignRolesToUsersRequest request =
        AssignRolesToUsersRequest.builder()
            .userIdentifiers(b -> b.id("123").id("456"))
            .roleIdentifiers(b -> b.id("101"))
            .build();

    service.assignRolesToUsers(request);

    verify(auditLogService, times(3)).createAuditLog(any(CreateAuditLogRequest.class));
  }
}
