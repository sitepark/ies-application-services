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
import com.sitepark.ies.userrepository.core.usecase.user.UnassignRolesFromUsersRequest;
import com.sitepark.ies.userrepository.core.usecase.user.UnassignRolesFromUsersResult;
import com.sitepark.ies.userrepository.core.usecase.user.UnassignRolesFromUsersUseCase;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UnassignRolesFromUsersServiceTest {

  private UnassignRolesFromUsersUseCase unassignRolesFromUsersUseCase;

  @SuppressWarnings("PMD.SingularField")
  private MultiEntityNameResolver multiEntityNameResolver;

  @SuppressWarnings("PMD.SingularField")
  private ApplicationAuditLogServiceFactory auditLogServiceFactory;

  private ApplicationAuditLogService auditLogService;
  private UnassignRolesFromUsersService service;

  @BeforeEach
  void setUp() {
    this.unassignRolesFromUsersUseCase = mock();
    this.multiEntityNameResolver = mock();
    this.auditLogServiceFactory = mock();
    this.auditLogService = mock();
    this.service =
        new UnassignRolesFromUsersService(
            unassignRolesFromUsersUseCase, multiEntityNameResolver, auditLogServiceFactory);
    when(auditLogServiceFactory.create(any(), any())).thenReturn(auditLogService);
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

    UnassignRoleFromUserServiceRequest request =
        UnassignRoleFromUserServiceRequest.builder()
            .unassignRolesFromUsersRequest(
                UnassignRolesFromUsersRequest.builder()
                    .userIdentifiers(b -> b.id("123"))
                    .roleIdentifiers(b -> b.id("101").id("102"))
                    .build())
            .build();

    service.unassignRolesFromUsers(request);

    verify(unassignRolesFromUsersUseCase)
        .unassignRolesFromUsers(any(UnassignRolesFromUsersRequest.class));
  }

  @Test
  void testUnassignRolesFromUsersCreatesAuditLogWhenUnassigned() throws Exception {

    UserRoleAssignment unassignments =
        UserRoleAssignment.builder().assignments("123", List.of("101", "102")).build();

    Instant timestamp = Instant.now();
    UnassignRolesFromUsersResult.Unassigned unassignedResult =
        new UnassignRolesFromUsersResult.Unassigned(unassignments, timestamp);

    when(unassignRolesFromUsersUseCase.unassignRolesFromUsers(
            any(UnassignRolesFromUsersRequest.class)))
        .thenReturn(unassignedResult);

    UnassignRoleFromUserServiceRequest request =
        UnassignRoleFromUserServiceRequest.builder()
            .unassignRolesFromUsersRequest(
                UnassignRolesFromUsersRequest.builder()
                    .userIdentifiers(b -> b.id("123"))
                    .roleIdentifiers(b -> b.id("101").id("102"))
                    .build())
            .build();

    service.unassignRolesFromUsers(request);

    verify(auditLogService).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testUnassignRolesFromUsersDoesNotCreateAuditLogWhenSkipped() {

    UserRoleAssignment emptyUnassignments = UserRoleAssignment.builder().build();
    UnassignRolesFromUsersResult.Skipped skippedResult =
        new UnassignRolesFromUsersResult.Skipped(emptyUnassignments);

    when(unassignRolesFromUsersUseCase.unassignRolesFromUsers(
            any(UnassignRolesFromUsersRequest.class)))
        .thenReturn(skippedResult);

    UnassignRoleFromUserServiceRequest request =
        UnassignRoleFromUserServiceRequest.builder()
            .unassignRolesFromUsersRequest(
                UnassignRolesFromUsersRequest.builder()
                    .userIdentifiers(b -> b.id("123"))
                    .roleIdentifiers(b -> b.id("101"))
                    .build())
            .build();

    service.unassignRolesFromUsers(request);
    verify(auditLogService, never()).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testUnassignRolesFromUsersCreatesBatchParentLogForMultipleUsers() throws Exception {

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

    UnassignRoleFromUserServiceRequest request =
        UnassignRoleFromUserServiceRequest.builder()
            .unassignRolesFromUsersRequest(
                UnassignRolesFromUsersRequest.builder()
                    .userIdentifiers(b -> b.id("123").id("456"))
                    .roleIdentifiers(b -> b.id("101"))
                    .build())
            .build();

    service.unassignRolesFromUsers(request);
    verify(auditLogService, times(2)).createLog(any(), any(), any(), any(), any());
  }
}
