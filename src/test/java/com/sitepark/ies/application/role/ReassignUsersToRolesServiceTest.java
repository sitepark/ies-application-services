package com.sitepark.ies.application.role;

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
import com.sitepark.ies.userrepository.core.domain.value.RoleUserAssignment;
import com.sitepark.ies.userrepository.core.usecase.role.ReassignUsersToRolesRequest;
import com.sitepark.ies.userrepository.core.usecase.role.ReassignUsersToRolesResult;
import com.sitepark.ies.userrepository.core.usecase.role.ReassignUsersToRolesUseCase;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReassignUsersToRolesServiceTest {

  private ReassignUsersToRolesUseCase reassignUsersToRolesUseCase;

  @SuppressWarnings("PMD.SingularField")
  private MultiEntityNameResolver multiEntityNameResolver;

  @SuppressWarnings("PMD.SingularField")
  private ApplicationAuditLogServiceFactory auditLogServiceFactory;

  private ApplicationAuditLogService auditLogService;
  private ReassignUsersToRolesService service;

  @BeforeEach
  void setUp() {
    this.reassignUsersToRolesUseCase = mock();
    this.multiEntityNameResolver = mock();
    this.auditLogServiceFactory = mock();
    this.auditLogService = mock();
    this.service =
        new ReassignUsersToRolesService(
            reassignUsersToRolesUseCase, multiEntityNameResolver, auditLogServiceFactory);
    when(auditLogServiceFactory.create(any(), any())).thenReturn(auditLogService);
    when(multiEntityNameResolver.resolveDisplayUserNames(any())).thenReturn(Map.of());
  }

  @Test
  void testReassignUsersToRolesCallsUseCase() {

    RoleUserAssignment assignments =
        RoleUserAssignment.builder().assignments("101", List.of("301")).build();
    RoleUserAssignment unassignments = RoleUserAssignment.builder().build();
    Instant timestamp = Instant.now();
    ReassignUsersToRolesResult.Reassigned reassignedResult =
        new ReassignUsersToRolesResult.Reassigned(assignments, unassignments, timestamp);

    when(reassignUsersToRolesUseCase.reassignUsersToRoles(any(ReassignUsersToRolesRequest.class)))
        .thenReturn(reassignedResult);

    ReassignUsersToRolesServiceRequest request =
        ReassignUsersToRolesServiceRequest.builder()
            .reassignRolesToUsersRequest(
                ReassignUsersToRolesRequest.builder()
                    .roleIdentifiers(b -> b.id("101"))
                    .userIdentifiers(b -> b.id("301"))
                    .build())
            .build();

    service.reassignUsersToRoles(request);

    verify(reassignUsersToRolesUseCase)
        .reassignUsersToRoles(any(ReassignUsersToRolesRequest.class));
  }

  @Test
  void testReassignUsersToRolesReturnsReassignResult() {

    RoleUserAssignment assignments =
        RoleUserAssignment.builder().assignments("101", List.of("301")).build();
    RoleUserAssignment unassignments =
        RoleUserAssignment.builder().assignments("102", List.of("301")).build();
    Instant timestamp = Instant.now();
    ReassignUsersToRolesResult.Reassigned reassignedResult =
        new ReassignUsersToRolesResult.Reassigned(assignments, unassignments, timestamp);

    when(reassignUsersToRolesUseCase.reassignUsersToRoles(any(ReassignUsersToRolesRequest.class)))
        .thenReturn(reassignedResult);

    ReassignUsersToRolesServiceRequest request =
        ReassignUsersToRolesServiceRequest.builder()
            .reassignRolesToUsersRequest(
                ReassignUsersToRolesRequest.builder()
                    .roleIdentifiers(b -> b.id("101"))
                    .userIdentifiers(b -> b.id("301"))
                    .build())
            .build();

    ReassignResult result = service.reassignUsersToRoles(request);

    assertEquals(
        new ReassignResult(1, 1),
        result,
        "Should return reassign result with assigned/unassigned counts");
  }

  @Test
  void testReassignUsersToRolesReturnsEmptyResultWhenSkipped() {

    ReassignUsersToRolesResult.Skipped skippedResult = new ReassignUsersToRolesResult.Skipped();

    when(reassignUsersToRolesUseCase.reassignUsersToRoles(any(ReassignUsersToRolesRequest.class)))
        .thenReturn(skippedResult);

    ReassignUsersToRolesServiceRequest request =
        ReassignUsersToRolesServiceRequest.builder()
            .reassignRolesToUsersRequest(
                ReassignUsersToRolesRequest.builder()
                    .roleIdentifiers(b -> b.id("101"))
                    .userIdentifiers(b -> b.id("301"))
                    .build())
            .build();

    ReassignResult result = service.reassignUsersToRoles(request);

    assertEquals(ReassignResult.empty(), result, "Should return empty result when skipped");
  }

  @Test
  void testReassignUsersToRolesCreatesAuditLogWhenReassigned() throws Exception {

    RoleUserAssignment assignments =
        RoleUserAssignment.builder().assignments("101", List.of("301")).build();
    RoleUserAssignment unassignments = RoleUserAssignment.builder().build();
    Instant timestamp = Instant.now();
    ReassignUsersToRolesResult.Reassigned reassignedResult =
        new ReassignUsersToRolesResult.Reassigned(assignments, unassignments, timestamp);

    when(reassignUsersToRolesUseCase.reassignUsersToRoles(any(ReassignUsersToRolesRequest.class)))
        .thenReturn(reassignedResult);

    ReassignUsersToRolesServiceRequest request =
        ReassignUsersToRolesServiceRequest.builder()
            .reassignRolesToUsersRequest(
                ReassignUsersToRolesRequest.builder()
                    .roleIdentifiers(b -> b.id("101"))
                    .userIdentifiers(b -> b.id("301"))
                    .build())
            .build();

    service.reassignUsersToRoles(request);

    verify(auditLogService).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testReassignUsersToRolesDoesNotCreateAuditLogWhenSkipped() {

    ReassignUsersToRolesResult.Skipped skippedResult = new ReassignUsersToRolesResult.Skipped();

    when(reassignUsersToRolesUseCase.reassignUsersToRoles(any(ReassignUsersToRolesRequest.class)))
        .thenReturn(skippedResult);

    ReassignUsersToRolesServiceRequest request =
        ReassignUsersToRolesServiceRequest.builder()
            .reassignRolesToUsersRequest(
                ReassignUsersToRolesRequest.builder()
                    .roleIdentifiers(b -> b.id("101"))
                    .userIdentifiers(b -> b.id("301"))
                    .build())
            .build();

    service.reassignUsersToRoles(request);

    verify(auditLogService, never()).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testReassignUsersToRolesCreatesBatchLogForMultipleChanges() throws Exception {

    RoleUserAssignment assignments =
        RoleUserAssignment.builder().assignments("101", List.of("301")).build();
    RoleUserAssignment unassignments =
        RoleUserAssignment.builder().assignments("102", List.of("302")).build();
    Instant timestamp = Instant.now();
    ReassignUsersToRolesResult.Reassigned reassignedResult =
        new ReassignUsersToRolesResult.Reassigned(assignments, unassignments, timestamp);

    when(reassignUsersToRolesUseCase.reassignUsersToRoles(any(ReassignUsersToRolesRequest.class)))
        .thenReturn(reassignedResult);

    ReassignUsersToRolesServiceRequest request =
        ReassignUsersToRolesServiceRequest.builder()
            .reassignRolesToUsersRequest(
                ReassignUsersToRolesRequest.builder()
                    .roleIdentifiers(b -> b.id("101"))
                    .userIdentifiers(b -> b.id("301"))
                    .build())
            .build();

    service.reassignUsersToRoles(request);

    verify(auditLogService, times(2)).createLog(any(), any(), any(), any(), any());
  }
}
