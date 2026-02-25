package com.sitepark.ies.application.privilege;

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
import com.sitepark.ies.userrepository.core.domain.value.PrivilegeRoleAssignment;
import com.sitepark.ies.userrepository.core.usecase.privilege.ReassignRolesToPrivilegesRequest;
import com.sitepark.ies.userrepository.core.usecase.privilege.ReassignRolesToPrivilegesResult;
import com.sitepark.ies.userrepository.core.usecase.privilege.ReassignRolesToPrivilegesUseCase;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReassignRolesToPrivilegesServiceTest {

  private ReassignRolesToPrivilegesUseCase reassignRolesToPrivilegesUseCase;

  @SuppressWarnings("PMD.SingularField")
  private MultiEntityNameResolver multiEntityNameResolver;

  @SuppressWarnings("PMD.SingularField")
  private ApplicationAuditLogServiceFactory auditLogServiceFactory;

  private ApplicationAuditLogService auditLogService;
  private ReassignRolesToPrivilegesService service;

  @BeforeEach
  void setUp() {
    this.reassignRolesToPrivilegesUseCase = mock();
    this.multiEntityNameResolver = mock();
    this.auditLogServiceFactory = mock();
    this.auditLogService = mock();
    this.service =
        new ReassignRolesToPrivilegesService(
            reassignRolesToPrivilegesUseCase, multiEntityNameResolver, auditLogServiceFactory);
    when(auditLogServiceFactory.create(any(), any())).thenReturn(auditLogService);
    when(multiEntityNameResolver.resolveRoleNames(any())).thenReturn(Map.of());
  }

  @Test
  void testReassignRolesToPrivilegesCallsUseCase() {

    PrivilegeRoleAssignment assignments =
        PrivilegeRoleAssignment.builder().assignments("201", List.of("101")).build();
    PrivilegeRoleAssignment unassignments = PrivilegeRoleAssignment.builder().build();
    Instant timestamp = Instant.now();
    ReassignRolesToPrivilegesResult.Reassigned reassignedResult =
        new ReassignRolesToPrivilegesResult.Reassigned(assignments, unassignments, timestamp);

    when(reassignRolesToPrivilegesUseCase.reassignRolesToPrivileges(
            any(ReassignRolesToPrivilegesRequest.class)))
        .thenReturn(reassignedResult);

    ReassignRolesToPrivilegesServiceRequest request =
        ReassignRolesToPrivilegesServiceRequest.builder()
            .reassignRolesToPrivilegesRequest(
                ReassignRolesToPrivilegesRequest.builder()
                    .roleIdentifiers(b -> b.id("101"))
                    .privilegeIdentifiers(b -> b.id("201"))
                    .build())
            .build();

    service.reassignRolesToPrivileges(request);

    verify(reassignRolesToPrivilegesUseCase)
        .reassignRolesToPrivileges(any(ReassignRolesToPrivilegesRequest.class));
  }

  @Test
  void testReassignRolesToPrivilegesReturnsReassignResult() {

    PrivilegeRoleAssignment assignments =
        PrivilegeRoleAssignment.builder().assignments("201", List.of("101")).build();
    PrivilegeRoleAssignment unassignments =
        PrivilegeRoleAssignment.builder().assignments("202", List.of("101")).build();
    Instant timestamp = Instant.now();
    ReassignRolesToPrivilegesResult.Reassigned reassignedResult =
        new ReassignRolesToPrivilegesResult.Reassigned(assignments, unassignments, timestamp);

    when(reassignRolesToPrivilegesUseCase.reassignRolesToPrivileges(
            any(ReassignRolesToPrivilegesRequest.class)))
        .thenReturn(reassignedResult);

    ReassignRolesToPrivilegesServiceRequest request =
        ReassignRolesToPrivilegesServiceRequest.builder()
            .reassignRolesToPrivilegesRequest(
                ReassignRolesToPrivilegesRequest.builder()
                    .roleIdentifiers(b -> b.id("101"))
                    .privilegeIdentifiers(b -> b.id("201"))
                    .build())
            .build();

    ReassignResult result = service.reassignRolesToPrivileges(request);

    assertEquals(
        new ReassignResult(1, 1),
        result,
        "Should return reassign result with assigned/unassigned counts");
  }

  @Test
  void testReassignRolesToPrivilegesReturnsEmptyResultWhenSkipped() {

    ReassignRolesToPrivilegesResult.Skipped skippedResult =
        new ReassignRolesToPrivilegesResult.Skipped();

    when(reassignRolesToPrivilegesUseCase.reassignRolesToPrivileges(
            any(ReassignRolesToPrivilegesRequest.class)))
        .thenReturn(skippedResult);

    ReassignRolesToPrivilegesServiceRequest request =
        ReassignRolesToPrivilegesServiceRequest.builder()
            .reassignRolesToPrivilegesRequest(
                ReassignRolesToPrivilegesRequest.builder()
                    .roleIdentifiers(b -> b.id("101"))
                    .privilegeIdentifiers(b -> b.id("201"))
                    .build())
            .build();

    ReassignResult result = service.reassignRolesToPrivileges(request);

    assertEquals(ReassignResult.empty(), result, "Should return empty result when skipped");
  }

  @Test
  void testReassignRolesToPrivilegesCreatesAuditLogWhenReassigned() throws Exception {

    PrivilegeRoleAssignment assignments =
        PrivilegeRoleAssignment.builder().assignments("201", List.of("101")).build();
    PrivilegeRoleAssignment unassignments = PrivilegeRoleAssignment.builder().build();
    Instant timestamp = Instant.now();
    ReassignRolesToPrivilegesResult.Reassigned reassignedResult =
        new ReassignRolesToPrivilegesResult.Reassigned(assignments, unassignments, timestamp);

    when(reassignRolesToPrivilegesUseCase.reassignRolesToPrivileges(
            any(ReassignRolesToPrivilegesRequest.class)))
        .thenReturn(reassignedResult);

    ReassignRolesToPrivilegesServiceRequest request =
        ReassignRolesToPrivilegesServiceRequest.builder()
            .reassignRolesToPrivilegesRequest(
                ReassignRolesToPrivilegesRequest.builder()
                    .roleIdentifiers(b -> b.id("101"))
                    .privilegeIdentifiers(b -> b.id("201"))
                    .build())
            .build();

    service.reassignRolesToPrivileges(request);

    verify(auditLogService).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testReassignRolesToPrivilegesDoesNotCreateAuditLogWhenSkipped() {

    ReassignRolesToPrivilegesResult.Skipped skippedResult =
        new ReassignRolesToPrivilegesResult.Skipped();

    when(reassignRolesToPrivilegesUseCase.reassignRolesToPrivileges(
            any(ReassignRolesToPrivilegesRequest.class)))
        .thenReturn(skippedResult);

    ReassignRolesToPrivilegesServiceRequest request =
        ReassignRolesToPrivilegesServiceRequest.builder()
            .reassignRolesToPrivilegesRequest(
                ReassignRolesToPrivilegesRequest.builder()
                    .roleIdentifiers(b -> b.id("101"))
                    .privilegeIdentifiers(b -> b.id("201"))
                    .build())
            .build();

    service.reassignRolesToPrivileges(request);

    verify(auditLogService, never()).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testReassignRolesToPrivilegesCreatesBatchLogForMultipleChanges() throws Exception {

    PrivilegeRoleAssignment assignments =
        PrivilegeRoleAssignment.builder().assignments("201", List.of("101")).build();
    PrivilegeRoleAssignment unassignments =
        PrivilegeRoleAssignment.builder().assignments("202", List.of("102")).build();
    Instant timestamp = Instant.now();
    ReassignRolesToPrivilegesResult.Reassigned reassignedResult =
        new ReassignRolesToPrivilegesResult.Reassigned(assignments, unassignments, timestamp);

    when(reassignRolesToPrivilegesUseCase.reassignRolesToPrivileges(
            any(ReassignRolesToPrivilegesRequest.class)))
        .thenReturn(reassignedResult);

    ReassignRolesToPrivilegesServiceRequest request =
        ReassignRolesToPrivilegesServiceRequest.builder()
            .reassignRolesToPrivilegesRequest(
                ReassignRolesToPrivilegesRequest.builder()
                    .roleIdentifiers(b -> b.id("101"))
                    .privilegeIdentifiers(b -> b.id("201"))
                    .build())
            .build();

    service.reassignRolesToPrivileges(request);

    verify(auditLogService, times(2)).createLog(any(), any(), any(), any(), any());
  }
}
