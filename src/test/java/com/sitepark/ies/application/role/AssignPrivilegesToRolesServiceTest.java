package com.sitepark.ies.application.role;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.MultiEntityNameResolver;
import com.sitepark.ies.userrepository.core.domain.value.RolePrivilegeAssignment;
import com.sitepark.ies.userrepository.core.usecase.role.AssignPrivilegesToRolesResult;
import com.sitepark.ies.userrepository.core.usecase.role.AssignPrivilegesToRolesUseCase;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AssignPrivilegesToRolesServiceTest {

  private AssignPrivilegesToRolesUseCase assignPrivilegesToRolesUseCase;

  @SuppressWarnings("PMD.SingularField")
  private MultiEntityNameResolver multiEntityNameResolver;

  @SuppressWarnings("PMD.SingularField")
  private ApplicationAuditLogServiceFactory auditLogServiceFactory;

  private ApplicationAuditLogService auditLogService;
  private AssignPrivilegesToRolesService service;

  @BeforeEach
  void setUp() {
    this.assignPrivilegesToRolesUseCase = mock();
    this.multiEntityNameResolver = mock();
    this.auditLogServiceFactory = mock();
    this.auditLogService = mock();
    this.service =
        new AssignPrivilegesToRolesService(
            assignPrivilegesToRolesUseCase, multiEntityNameResolver, auditLogServiceFactory);
    when(auditLogServiceFactory.create(any(), any())).thenReturn(auditLogService);
  }

  @Test
  void testAssignPrivilegesToRolesCallsUseCase() {

    RolePrivilegeAssignment assignments =
        RolePrivilegeAssignment.builder().assignments("101", List.of("201", "202")).build();
    Instant timestamp = Instant.now();
    AssignPrivilegesToRolesResult.Assigned assignedResult =
        new AssignPrivilegesToRolesResult.Assigned(assignments, timestamp);

    when(assignPrivilegesToRolesUseCase.assignPrivilegesToRoles(
            any(
                com.sitepark.ies.userrepository.core.usecase.role.AssignPrivilegesToRolesRequest
                    .class)))
        .thenReturn(assignedResult);

    AssignPrivilegesToRolesServiceRequest request =
        AssignPrivilegesToRolesServiceRequest.builder()
            .assignPrivilegesToRolesRequest(
                com.sitepark.ies.userrepository.core.usecase.role.AssignPrivilegesToRolesRequest
                    .builder()
                    .roleIdentifiers(b -> b.id("101"))
                    .privilegeIdentifiers(b -> b.id("201").id("202"))
                    .build())
            .build();

    service.assignPrivilegesToRoles(request);

    verify(assignPrivilegesToRolesUseCase)
        .assignPrivilegesToRoles(
            any(
                com.sitepark.ies.userrepository.core.usecase.role.AssignPrivilegesToRolesRequest
                    .class));
  }

  @Test
  void testAssignPrivilegesToRolesCreatesAuditLogWhenAssigned() throws Exception {

    RolePrivilegeAssignment assignments =
        RolePrivilegeAssignment.builder().assignments("101", List.of("201", "202")).build();
    Instant timestamp = Instant.now();
    AssignPrivilegesToRolesResult.Assigned assignedResult =
        new AssignPrivilegesToRolesResult.Assigned(assignments, timestamp);

    when(assignPrivilegesToRolesUseCase.assignPrivilegesToRoles(
            any(
                com.sitepark.ies.userrepository.core.usecase.role.AssignPrivilegesToRolesRequest
                    .class)))
        .thenReturn(assignedResult);

    AssignPrivilegesToRolesServiceRequest request =
        AssignPrivilegesToRolesServiceRequest.builder()
            .assignPrivilegesToRolesRequest(
                com.sitepark.ies.userrepository.core.usecase.role.AssignPrivilegesToRolesRequest
                    .builder()
                    .roleIdentifiers(b -> b.id("101"))
                    .privilegeIdentifiers(b -> b.id("201").id("202"))
                    .build())
            .build();

    service.assignPrivilegesToRoles(request);

    verify(auditLogService).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testAssignPrivilegesToRolesDoesNotCreateAuditLogWhenSkipped() {

    AssignPrivilegesToRolesResult.Skipped skippedResult =
        new AssignPrivilegesToRolesResult.Skipped();

    when(assignPrivilegesToRolesUseCase.assignPrivilegesToRoles(
            any(
                com.sitepark.ies.userrepository.core.usecase.role.AssignPrivilegesToRolesRequest
                    .class)))
        .thenReturn(skippedResult);

    AssignPrivilegesToRolesServiceRequest request =
        AssignPrivilegesToRolesServiceRequest.builder()
            .assignPrivilegesToRolesRequest(
                com.sitepark.ies.userrepository.core.usecase.role.AssignPrivilegesToRolesRequest
                    .builder()
                    .roleIdentifiers(b -> b.id("101"))
                    .privilegeIdentifiers(b -> b.id("201"))
                    .build())
            .build();

    service.assignPrivilegesToRoles(request);

    verify(auditLogService, never()).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testAssignPrivilegesToRolesCreatesBatchParentLogForMultipleRoles() throws Exception {

    RolePrivilegeAssignment assignments =
        RolePrivilegeAssignment.builder()
            .assignments("101", List.of("201"))
            .assignments("102", List.of("201"))
            .build();
    Instant timestamp = Instant.now();
    AssignPrivilegesToRolesResult.Assigned assignedResult =
        new AssignPrivilegesToRolesResult.Assigned(assignments, timestamp);

    when(assignPrivilegesToRolesUseCase.assignPrivilegesToRoles(
            any(
                com.sitepark.ies.userrepository.core.usecase.role.AssignPrivilegesToRolesRequest
                    .class)))
        .thenReturn(assignedResult);

    AssignPrivilegesToRolesServiceRequest request =
        AssignPrivilegesToRolesServiceRequest.builder()
            .assignPrivilegesToRolesRequest(
                com.sitepark.ies.userrepository.core.usecase.role.AssignPrivilegesToRolesRequest
                    .builder()
                    .roleIdentifiers(b -> b.id("101").id("102"))
                    .privilegeIdentifiers(b -> b.id("201"))
                    .build())
            .build();

    service.assignPrivilegesToRoles(request);

    verify(auditLogService, times(2)).createLog(any(), any(), any(), any(), any());
  }
}
