package com.sitepark.ies.application.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.MultiEntityNameResolver;
import com.sitepark.ies.userrepository.core.domain.value.UserRoleAssignment;
import com.sitepark.ies.userrepository.core.usecase.user.AssignRolesToUsersResult;
import com.sitepark.ies.userrepository.core.usecase.user.AssignRolesToUsersUseCase;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AssignRolesToUsersServiceTest {

  private AssignRolesToUsersUseCase assignRolesToUsersUseCase;

  @SuppressWarnings("PMD.SingularField")
  private MultiEntityNameResolver multiEntityNameResolver;

  @SuppressWarnings("PMD.SingularField")
  private ApplicationAuditLogServiceFactory auditLogServiceFactory;

  private ApplicationAuditLogService auditLogService;

  private AssignRolesToUsersService service;

  @BeforeEach
  void setUp() {
    this.assignRolesToUsersUseCase = mock();
    this.multiEntityNameResolver = mock();
    this.auditLogServiceFactory = mock();
    this.auditLogService = mock();
    this.service =
        new AssignRolesToUsersService(
            assignRolesToUsersUseCase, multiEntityNameResolver, auditLogServiceFactory);
    when(auditLogServiceFactory.create(any(), any())).thenReturn(auditLogService);
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

    AssignRolesToUsersServiceRequest request =
        AssignRolesToUsersServiceRequest.builder()
            .assignRolesToUsersRequest(
                com.sitepark.ies.userrepository.core.usecase.user.AssignRolesToUsersRequest
                    .builder()
                    .userIdentifiers(b -> b.id("123"))
                    .roleIdentifiers(b -> b.id("101").id("102"))
                    .build())
            .build();

    service.assignRolesToUsers(request);

    verify(assignRolesToUsersUseCase)
        .assignRolesToUsers(
            any(com.sitepark.ies.userrepository.core.usecase.user.AssignRolesToUsersRequest.class));
  }

  @Test
  void testAssignRolesToUsersCreatesAuditLogWhenAssigned() throws Exception {

    UserRoleAssignment assignments =
        UserRoleAssignment.builder().assignments("123", List.of("101", "102")).build();

    Instant timestamp = Instant.now();
    AssignRolesToUsersResult.Assigned assignedResult =
        new AssignRolesToUsersResult.Assigned(assignments, timestamp);

    when(assignRolesToUsersUseCase.assignRolesToUsers(
            any(com.sitepark.ies.userrepository.core.usecase.user.AssignRolesToUsersRequest.class)))
        .thenReturn(assignedResult);

    AssignRolesToUsersServiceRequest request =
        AssignRolesToUsersServiceRequest.builder()
            .assignRolesToUsersRequest(
                com.sitepark.ies.userrepository.core.usecase.user.AssignRolesToUsersRequest
                    .builder()
                    .userIdentifiers(b -> b.id("123"))
                    .roleIdentifiers(b -> b.id("101").id("102"))
                    .build())
            .build();

    service.assignRolesToUsers(request);

    verify(auditLogService).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testAssignRolesToUsersDoesNotCreateAuditLogWhenSkipped() {

    UserRoleAssignment emptyAssignments = UserRoleAssignment.builder().build();
    AssignRolesToUsersResult.Skipped skippedResult =
        new AssignRolesToUsersResult.Skipped(emptyAssignments);

    when(assignRolesToUsersUseCase.assignRolesToUsers(
            any(com.sitepark.ies.userrepository.core.usecase.user.AssignRolesToUsersRequest.class)))
        .thenReturn(skippedResult);

    AssignRolesToUsersServiceRequest request =
        AssignRolesToUsersServiceRequest.builder()
            .assignRolesToUsersRequest(
                com.sitepark.ies.userrepository.core.usecase.user.AssignRolesToUsersRequest
                    .builder()
                    .userIdentifiers(b -> b.id("123"))
                    .roleIdentifiers(b -> b.id("101"))
                    .build())
            .build();

    service.assignRolesToUsers(request);

    verify(auditLogService, never()).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testAssignRolesToUsersCreatesBatchParentLogForMultipleUsers() throws Exception {

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

    AssignRolesToUsersServiceRequest request =
        AssignRolesToUsersServiceRequest.builder()
            .assignRolesToUsersRequest(
                com.sitepark.ies.userrepository.core.usecase.user.AssignRolesToUsersRequest
                    .builder()
                    .userIdentifiers(b -> b.id("123").id("456"))
                    .roleIdentifiers(b -> b.id("101"))
                    .build())
            .build();

    service.assignRolesToUsers(request);

    verify(auditLogService, times(2)).createLog(any(), any(), any(), any(), any());
  }
}
