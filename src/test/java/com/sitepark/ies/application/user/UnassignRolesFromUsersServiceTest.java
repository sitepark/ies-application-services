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
import com.sitepark.ies.userrepository.core.usecase.user.UnassignRolesFromUsersRequest;
import com.sitepark.ies.userrepository.core.usecase.user.UnassignRolesFromUsersResult;
import com.sitepark.ies.userrepository.core.usecase.user.UnassignRolesFromUsersUseCase;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UnassignRolesFromUsersServiceTest {

  private UnassignRolesFromUsersUseCase unassignRolesFromUsersUseCase;
  private UserRepository userRepository;
  private AuditLogService auditLogService;
  private UnassignRolesFromUsersService service;

  @BeforeEach
  void setUp() {
    this.unassignRolesFromUsersUseCase = mock();
    this.userRepository = mock();
    this.auditLogService = mock();
    this.service =
        new UnassignRolesFromUsersService(
            unassignRolesFromUsersUseCase, userRepository, auditLogService);
  }

  @Test
  void testUnassignRolesFromUsersCallsUseCase() {

    UserRoleAssignment unassignments =
        UserRoleAssignment.builder().assignments("123", List.of("101", "102")).build();

    Instant timestamp = Instant.now();
    UnassignRolesFromUsersResult.Unassigned unassignedResult =
        new UnassignRolesFromUsersResult.Unassigned(unassignments, timestamp);

    when(unassignRolesFromUsersUseCase.unassignRolesFromUsers(
            any(UnassignRolesFromUsersRequest.class)))
        .thenReturn(unassignedResult);

    UnassignRolesFromUsersRequest request =
        UnassignRolesFromUsersRequest.builder()
            .userIdentifiers(b -> b.id("123"))
            .roleIdentifiers(b -> b.id("101").id("102"))
            .build();

    service.unassignRolesFromUsers(request);

    verify(unassignRolesFromUsersUseCase)
        .unassignRolesFromUsers(any(UnassignRolesFromUsersRequest.class));
  }

  @Test
  void testUnassignRolesFromUsersCreatesAuditLogWhenUnassigned() throws Exception {

    User user = User.builder().id("123").login("testuser").lastName("Test").build();
    UserRoleAssignment unassignments =
        UserRoleAssignment.builder().assignments("123", List.of("101", "102")).build();

    Instant timestamp = Instant.now();
    UnassignRolesFromUsersResult.Unassigned unassignedResult =
        new UnassignRolesFromUsersResult.Unassigned(unassignments, timestamp);

    when(unassignRolesFromUsersUseCase.unassignRolesFromUsers(
            any(UnassignRolesFromUsersRequest.class)))
        .thenReturn(unassignedResult);
    when(userRepository.get("123")).thenReturn(Optional.of(user));
    when(auditLogService.serialize(any())).thenReturn("[\"101\",\"102\"]");

    UnassignRolesFromUsersRequest request =
        UnassignRolesFromUsersRequest.builder()
            .userIdentifiers(b -> b.id("123"))
            .roleIdentifiers(b -> b.id("101").id("102"))
            .build();

    service.unassignRolesFromUsers(request);

    verify(auditLogService).createAuditLog(any(CreateAuditLogRequest.class));
  }

  @Test
  void testUnassignRolesFromUsersDoesNotCreateAuditLogWhenSkipped() {

    UserRoleAssignment emptyUnassignments = UserRoleAssignment.builder().build();
    UnassignRolesFromUsersResult.Skipped skippedResult =
        new UnassignRolesFromUsersResult.Skipped(emptyUnassignments);

    when(unassignRolesFromUsersUseCase.unassignRolesFromUsers(
            any(UnassignRolesFromUsersRequest.class)))
        .thenReturn(skippedResult);

    UnassignRolesFromUsersRequest request =
        UnassignRolesFromUsersRequest.builder()
            .userIdentifiers(b -> b.id("123"))
            .roleIdentifiers(b -> b.id("101"))
            .build();

    service.unassignRolesFromUsers(request);

    verify(auditLogService, never()).createAuditLog(any(CreateAuditLogRequest.class));
  }

  @Test
  void testUnassignRolesFromUsersCreatesBatchParentLogForMultipleUsers() throws Exception {

    User user1 = User.builder().id("123").login("user1").lastName("Test1").build();
    User user2 = User.builder().id("456").login("user2").lastName("Test2").build();

    UserRoleAssignment unassignments =
        UserRoleAssignment.builder()
            .assignments("123", List.of("101"))
            .assignments("456", List.of("101"))
            .build();

    Instant timestamp = Instant.now();
    UnassignRolesFromUsersResult.Unassigned unassignedResult =
        new UnassignRolesFromUsersResult.Unassigned(unassignments, timestamp);

    when(unassignRolesFromUsersUseCase.unassignRolesFromUsers(
            any(UnassignRolesFromUsersRequest.class)))
        .thenReturn(unassignedResult);
    when(userRepository.get("123")).thenReturn(Optional.of(user1));
    when(userRepository.get("456")).thenReturn(Optional.of(user2));
    when(auditLogService.serialize(any())).thenReturn("[\"101\"]");
    when(auditLogService.createAuditLog(any(CreateAuditLogRequest.class)))
        .thenReturn("batch-parent-id", "audit-1", "audit-2");

    UnassignRolesFromUsersRequest request =
        UnassignRolesFromUsersRequest.builder()
            .userIdentifiers(b -> b.id("123").id("456"))
            .roleIdentifiers(b -> b.id("101"))
            .build();

    service.unassignRolesFromUsers(request);

    verify(auditLogService, times(3)).createAuditLog(any(CreateAuditLogRequest.class));
  }
}
