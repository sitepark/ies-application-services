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
import com.sitepark.ies.userrepository.core.domain.value.RolePrivilegeAssignment;
import com.sitepark.ies.userrepository.core.usecase.role.ReassignPrivilegesToRolesResult;
import com.sitepark.ies.userrepository.core.usecase.role.ReassignPrivilegesToRolesUseCase;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReassignPrivilegesToRolesServiceTest {

  private ReassignPrivilegesToRolesUseCase reassignPrivilegesToRolesUseCase;

  @SuppressWarnings("PMD.SingularField")
  private MultiEntityNameResolver multiEntityNameResolver;

  @SuppressWarnings("PMD.SingularField")
  private ApplicationAuditLogServiceFactory auditLogServiceFactory;

  private ApplicationAuditLogService auditLogService;
  private ReassignPrivilegesToRolesService service;

  @BeforeEach
  void setUp() {
    this.reassignPrivilegesToRolesUseCase = mock();
    this.multiEntityNameResolver = mock();
    this.auditLogServiceFactory = mock();
    this.auditLogService = mock();
    this.service =
        new ReassignPrivilegesToRolesService(
            reassignPrivilegesToRolesUseCase, multiEntityNameResolver, auditLogServiceFactory);
    when(auditLogServiceFactory.create(any(), any())).thenReturn(auditLogService);
  }

  @Test
  void testReassignPrivilegesToRolesCallsUseCase() {

    RolePrivilegeAssignment assignments =
        RolePrivilegeAssignment.builder().assignments("101", List.of("201")).build();
    RolePrivilegeAssignment unassignments = RolePrivilegeAssignment.builder().build();
    Instant timestamp = Instant.now();
    ReassignPrivilegesToRolesResult.Reassigned reassignedResult =
        new ReassignPrivilegesToRolesResult.Reassigned(assignments, unassignments, timestamp);

    when(reassignPrivilegesToRolesUseCase.reassignPrivilegesToRoles(
            any(
                com.sitepark.ies.userrepository.core.usecase.role.ReassignPrivilegesToRolesRequest
                    .class)))
        .thenReturn(reassignedResult);

    ReassignPrivilegesToRolesServiceRequest request =
        ReassignPrivilegesToRolesServiceRequest.builder()
            .reassignPrivilegesToRolesRequest(
                com.sitepark.ies.userrepository.core.usecase.role.ReassignPrivilegesToRolesRequest
                    .builder()
                    .roleIdentifiers(b -> b.id("101"))
                    .privilegeIdentifiers(b -> b.id("201"))
                    .build())
            .build();

    service.reassignPrivilegesToRoles(request);

    verify(reassignPrivilegesToRolesUseCase)
        .reassignPrivilegesToRoles(
            any(
                com.sitepark.ies.userrepository.core.usecase.role.ReassignPrivilegesToRolesRequest
                    .class));
  }

  @Test
  void testReassignPrivilegesToRolesReturnsReassignResult() {

    RolePrivilegeAssignment assignments =
        RolePrivilegeAssignment.builder().assignments("101", List.of("201")).build();
    RolePrivilegeAssignment unassignments =
        RolePrivilegeAssignment.builder().assignments("101", List.of("202")).build();
    Instant timestamp = Instant.now();
    ReassignPrivilegesToRolesResult.Reassigned reassignedResult =
        new ReassignPrivilegesToRolesResult.Reassigned(assignments, unassignments, timestamp);

    when(reassignPrivilegesToRolesUseCase.reassignPrivilegesToRoles(
            any(
                com.sitepark.ies.userrepository.core.usecase.role.ReassignPrivilegesToRolesRequest
                    .class)))
        .thenReturn(reassignedResult);

    ReassignPrivilegesToRolesServiceRequest request =
        ReassignPrivilegesToRolesServiceRequest.builder()
            .reassignPrivilegesToRolesRequest(
                com.sitepark.ies.userrepository.core.usecase.role.ReassignPrivilegesToRolesRequest
                    .builder()
                    .roleIdentifiers(b -> b.id("101"))
                    .privilegeIdentifiers(b -> b.id("201"))
                    .build())
            .build();

    ReassignResult result = service.reassignPrivilegesToRoles(request);

    assertEquals(new ReassignResult(1, 1), result, "Should return reassign result with counts");
  }

  @Test
  void testReassignPrivilegesToRolesReturnsEmptyResultWhenSkipped() {

    ReassignPrivilegesToRolesResult.Skipped skippedResult =
        new ReassignPrivilegesToRolesResult.Skipped();

    when(reassignPrivilegesToRolesUseCase.reassignPrivilegesToRoles(
            any(
                com.sitepark.ies.userrepository.core.usecase.role.ReassignPrivilegesToRolesRequest
                    .class)))
        .thenReturn(skippedResult);

    ReassignPrivilegesToRolesServiceRequest request =
        ReassignPrivilegesToRolesServiceRequest.builder()
            .reassignPrivilegesToRolesRequest(
                com.sitepark.ies.userrepository.core.usecase.role.ReassignPrivilegesToRolesRequest
                    .builder()
                    .roleIdentifiers(b -> b.id("101"))
                    .privilegeIdentifiers(b -> b.id("201"))
                    .build())
            .build();

    ReassignResult result = service.reassignPrivilegesToRoles(request);

    assertEquals(ReassignResult.empty(), result, "Should return empty result when skipped");
  }

  @Test
  void testReassignPrivilegesToRolesCreatesAuditLogsWhenReassigned() throws Exception {

    RolePrivilegeAssignment assignments =
        RolePrivilegeAssignment.builder().assignments("101", List.of("201")).build();
    RolePrivilegeAssignment unassignments = RolePrivilegeAssignment.builder().build();
    Instant timestamp = Instant.now();
    ReassignPrivilegesToRolesResult.Reassigned reassignedResult =
        new ReassignPrivilegesToRolesResult.Reassigned(assignments, unassignments, timestamp);

    when(reassignPrivilegesToRolesUseCase.reassignPrivilegesToRoles(
            any(
                com.sitepark.ies.userrepository.core.usecase.role.ReassignPrivilegesToRolesRequest
                    .class)))
        .thenReturn(reassignedResult);

    ReassignPrivilegesToRolesServiceRequest request =
        ReassignPrivilegesToRolesServiceRequest.builder()
            .reassignPrivilegesToRolesRequest(
                com.sitepark.ies.userrepository.core.usecase.role.ReassignPrivilegesToRolesRequest
                    .builder()
                    .roleIdentifiers(b -> b.id("101"))
                    .privilegeIdentifiers(b -> b.id("201"))
                    .build())
            .build();

    service.reassignPrivilegesToRoles(request);

    verify(auditLogService).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testReassignPrivilegesToRolesDoesNotCreateAuditLogWhenSkipped() {

    ReassignPrivilegesToRolesResult.Skipped skippedResult =
        new ReassignPrivilegesToRolesResult.Skipped();

    when(reassignPrivilegesToRolesUseCase.reassignPrivilegesToRoles(
            any(
                com.sitepark.ies.userrepository.core.usecase.role.ReassignPrivilegesToRolesRequest
                    .class)))
        .thenReturn(skippedResult);

    ReassignPrivilegesToRolesServiceRequest request =
        ReassignPrivilegesToRolesServiceRequest.builder()
            .reassignPrivilegesToRolesRequest(
                com.sitepark.ies.userrepository.core.usecase.role.ReassignPrivilegesToRolesRequest
                    .builder()
                    .roleIdentifiers(b -> b.id("101"))
                    .privilegeIdentifiers(b -> b.id("201"))
                    .build())
            .build();

    service.reassignPrivilegesToRoles(request);

    verify(auditLogService, never()).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testReassignPrivilegesToRolesCreatesBatchLogForMultipleChanges() throws Exception {

    RolePrivilegeAssignment assignments =
        RolePrivilegeAssignment.builder().assignments("101", List.of("201")).build();
    RolePrivilegeAssignment unassignments =
        RolePrivilegeAssignment.builder().assignments("102", List.of("202")).build();
    Instant timestamp = Instant.now();
    ReassignPrivilegesToRolesResult.Reassigned reassignedResult =
        new ReassignPrivilegesToRolesResult.Reassigned(assignments, unassignments, timestamp);

    when(reassignPrivilegesToRolesUseCase.reassignPrivilegesToRoles(
            any(
                com.sitepark.ies.userrepository.core.usecase.role.ReassignPrivilegesToRolesRequest
                    .class)))
        .thenReturn(reassignedResult);

    ReassignPrivilegesToRolesServiceRequest request =
        ReassignPrivilegesToRolesServiceRequest.builder()
            .reassignPrivilegesToRolesRequest(
                com.sitepark.ies.userrepository.core.usecase.role.ReassignPrivilegesToRolesRequest
                    .builder()
                    .roleIdentifiers(b -> b.id("101"))
                    .privilegeIdentifiers(b -> b.id("201"))
                    .build())
            .build();

    service.reassignPrivilegesToRoles(request);

    verify(auditLogService, times(2)).createLog(any(), any(), any(), any(), any());
  }
}
