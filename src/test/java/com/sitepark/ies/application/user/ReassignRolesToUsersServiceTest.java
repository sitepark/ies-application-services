package com.sitepark.ies.application.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.MultiEntityNameResolver;
import com.sitepark.ies.application.value.ReassignResult;
import com.sitepark.ies.userrepository.core.domain.value.UserRoleAssignment;
import com.sitepark.ies.userrepository.core.usecase.user.ReassignRolesToUsersRequest;
import com.sitepark.ies.userrepository.core.usecase.user.ReassignRolesToUsersResult;
import com.sitepark.ies.userrepository.core.usecase.user.ReassignRolesToUsersUseCase;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReassignRolesToUsersServiceTest {

  private ReassignRolesToUsersUseCase reassignRolesToUsersUseCase;

  @SuppressWarnings("PMD.SingularField")
  private MultiEntityNameResolver multiEntityNameResolver;

  @SuppressWarnings("PMD.SingularField")
  private ApplicationAuditLogServiceFactory auditLogServiceFactory;

  private ApplicationAuditLogService auditLogService;
  private ReassignRolesToUsersService service;

  @BeforeEach
  void setUp() {
    this.reassignRolesToUsersUseCase = mock();
    this.multiEntityNameResolver = mock();
    this.auditLogServiceFactory = mock();
    this.auditLogService = mock();
    this.service =
        new ReassignRolesToUsersService(
            reassignRolesToUsersUseCase, multiEntityNameResolver, auditLogServiceFactory);
    when(auditLogServiceFactory.create(any(), any())).thenReturn(auditLogService);
  }

  @Test
  void testReassignRolesToUsersCallsUseCase() {

    UserRoleAssignment assignments =
        UserRoleAssignment.builder().assignments("301", List.of("101")).build();
    UserRoleAssignment unassignments = UserRoleAssignment.builder().build();
    Instant timestamp = Instant.now();
    ReassignRolesToUsersResult.Reassigned reassignedResult =
        new ReassignRolesToUsersResult.Reassigned(assignments, unassignments, timestamp);

    when(reassignRolesToUsersUseCase.reassignRolesToUsers(any(ReassignRolesToUsersRequest.class)))
        .thenReturn(reassignedResult);

    ReassignRolesToUsersServiceRequest request =
        ReassignRolesToUsersServiceRequest.builder()
            .reassignRolesToUsersRequest(
                ReassignRolesToUsersRequest.builder()
                    .userIdentifiers(b -> b.id("301"))
                    .roleIdentifiers(b -> b.id("101"))
                    .build())
            .build();

    service.reassignRolesToUsers(request);

    verify(reassignRolesToUsersUseCase)
        .reassignRolesToUsers(any(ReassignRolesToUsersRequest.class));
  }

  @Test
  void testReassignRolesToUsersReturnsReassignResult() {

    UserRoleAssignment assignments =
        UserRoleAssignment.builder().assignments("301", List.of("101")).build();
    UserRoleAssignment unassignments =
        UserRoleAssignment.builder().assignments("301", List.of("102")).build();
    Instant timestamp = Instant.now();
    ReassignRolesToUsersResult.Reassigned reassignedResult =
        new ReassignRolesToUsersResult.Reassigned(assignments, unassignments, timestamp);

    when(reassignRolesToUsersUseCase.reassignRolesToUsers(any(ReassignRolesToUsersRequest.class)))
        .thenReturn(reassignedResult);

    ReassignRolesToUsersServiceRequest request =
        ReassignRolesToUsersServiceRequest.builder()
            .reassignRolesToUsersRequest(
                ReassignRolesToUsersRequest.builder()
                    .userIdentifiers(b -> b.id("301"))
                    .roleIdentifiers(b -> b.id("101"))
                    .build())
            .build();

    ReassignResult result = service.reassignRolesToUsers(request);

    assertEquals(new ReassignResult(1, 1), result, "Should return reassign result with counts");
  }

  @Test
  void testReassignRolesToUsersReturnsEmptyResultWhenSkipped() {

    ReassignRolesToUsersResult.Skipped skippedResult = new ReassignRolesToUsersResult.Skipped();

    when(reassignRolesToUsersUseCase.reassignRolesToUsers(any(ReassignRolesToUsersRequest.class)))
        .thenReturn(skippedResult);

    ReassignRolesToUsersServiceRequest request =
        ReassignRolesToUsersServiceRequest.builder()
            .reassignRolesToUsersRequest(
                ReassignRolesToUsersRequest.builder()
                    .userIdentifiers(b -> b.id("301"))
                    .roleIdentifiers(b -> b.id("101"))
                    .build())
            .build();

    ReassignResult result = service.reassignRolesToUsers(request);

    assertEquals(ReassignResult.empty(), result, "Should return empty result when skipped");
  }

  @Test
  void testReassignRolesToUsersCreatesAuditLogWhenReassigned() throws Exception {

    UserRoleAssignment assignments =
        UserRoleAssignment.builder().assignments("301", List.of("101")).build();
    UserRoleAssignment unassignments = UserRoleAssignment.builder().build();
    Instant timestamp = Instant.now();
    ReassignRolesToUsersResult.Reassigned reassignedResult =
        new ReassignRolesToUsersResult.Reassigned(assignments, unassignments, timestamp);

    when(reassignRolesToUsersUseCase.reassignRolesToUsers(any(ReassignRolesToUsersRequest.class)))
        .thenReturn(reassignedResult);

    ReassignRolesToUsersServiceRequest request =
        ReassignRolesToUsersServiceRequest.builder()
            .reassignRolesToUsersRequest(
                ReassignRolesToUsersRequest.builder()
                    .userIdentifiers(b -> b.id("301"))
                    .roleIdentifiers(b -> b.id("101"))
                    .build())
            .build();

    service.reassignRolesToUsers(request);

    verify(auditLogService).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testReassignRolesToUsersDoesNotCreateAuditLogWhenSkipped() {

    ReassignRolesToUsersResult.Skipped skippedResult = new ReassignRolesToUsersResult.Skipped();

    when(reassignRolesToUsersUseCase.reassignRolesToUsers(any(ReassignRolesToUsersRequest.class)))
        .thenReturn(skippedResult);

    ReassignRolesToUsersServiceRequest request =
        ReassignRolesToUsersServiceRequest.builder()
            .reassignRolesToUsersRequest(
                ReassignRolesToUsersRequest.builder()
                    .userIdentifiers(b -> b.id("301"))
                    .roleIdentifiers(b -> b.id("101"))
                    .build())
            .build();

    service.reassignRolesToUsers(request);

    verify(auditLogService, never()).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testReassignRolesToUsersCreatesBatchLogForMultipleChanges() throws Exception {

    UserRoleAssignment assignments =
        UserRoleAssignment.builder().assignments("301", List.of("101")).build();
    UserRoleAssignment unassignments =
        UserRoleAssignment.builder().assignments("302", List.of("102")).build();
    Instant timestamp = Instant.now();
    ReassignRolesToUsersResult.Reassigned reassignedResult =
        new ReassignRolesToUsersResult.Reassigned(assignments, unassignments, timestamp);

    when(reassignRolesToUsersUseCase.reassignRolesToUsers(any(ReassignRolesToUsersRequest.class)))
        .thenReturn(reassignedResult);

    ReassignRolesToUsersServiceRequest request =
        ReassignRolesToUsersServiceRequest.builder()
            .reassignRolesToUsersRequest(
                ReassignRolesToUsersRequest.builder()
                    .userIdentifiers(b -> b.id("301"))
                    .roleIdentifiers(b -> b.id("101"))
                    .build())
            .build();

    service.reassignRolesToUsers(request);

    verify(auditLogService, times(2)).createLog(any(), any(), any(), any(), any());
  }
}
