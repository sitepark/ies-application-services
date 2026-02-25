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
import com.sitepark.ies.userrepository.core.usecase.role.UnassignPrivilegesFromRolesRequest;
import com.sitepark.ies.userrepository.core.usecase.role.UnassignPrivilegesFromRolesResult;
import com.sitepark.ies.userrepository.core.usecase.role.UnassignPrivilegesFromRolesUseCase;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UnassignPrivilegesFromRolesServiceTest {

  private UnassignPrivilegesFromRolesUseCase unassignPrivilegesFromRolesUseCase;

  @SuppressWarnings("PMD.SingularField")
  private MultiEntityNameResolver multiEntityNameResolver;

  @SuppressWarnings("PMD.SingularField")
  private ApplicationAuditLogServiceFactory auditLogServiceFactory;

  private ApplicationAuditLogService auditLogService;
  private UnassignPrivilegesFromRolesService service;

  @BeforeEach
  void setUp() {
    this.unassignPrivilegesFromRolesUseCase = mock();
    this.multiEntityNameResolver = mock();
    this.auditLogServiceFactory = mock();
    this.auditLogService = mock();
    this.service =
        new UnassignPrivilegesFromRolesService(
            unassignPrivilegesFromRolesUseCase, multiEntityNameResolver, auditLogServiceFactory);
    when(auditLogServiceFactory.create(any(), any())).thenReturn(auditLogService);
  }

  @Test
  void testUnassignPrivilegesFromRolesCallsUseCase() {

    RolePrivilegeAssignment unassignments =
        RolePrivilegeAssignment.builder().assignments("101", List.of("201", "202")).build();
    Instant timestamp = Instant.now();
    UnassignPrivilegesFromRolesResult.Unassigned unassignedResult =
        new UnassignPrivilegesFromRolesResult.Unassigned(unassignments, timestamp);

    when(unassignPrivilegesFromRolesUseCase.unassignPrivilegesFromRoles(
            any(UnassignPrivilegesFromRolesRequest.class)))
        .thenReturn(unassignedResult);

    UnassignPrivilegesFromRolesServiceRequest request =
        UnassignPrivilegesFromRolesServiceRequest.builder()
            .unassignPrivilegesFromRolesRequest(
                UnassignPrivilegesFromRolesRequest.builder()
                    .roleIdentifiers(b -> b.id("101"))
                    .privilegeIdentifiers(b -> b.id("201").id("202"))
                    .build())
            .build();

    service.unassignPrivilegesFromRoles(request);

    verify(unassignPrivilegesFromRolesUseCase)
        .unassignPrivilegesFromRoles(any(UnassignPrivilegesFromRolesRequest.class));
  }

  @Test
  void testUnassignPrivilegesFromRolesCreatesAuditLogWhenUnassigned() throws Exception {

    RolePrivilegeAssignment unassignments =
        RolePrivilegeAssignment.builder().assignments("101", List.of("201", "202")).build();
    Instant timestamp = Instant.now();
    UnassignPrivilegesFromRolesResult.Unassigned unassignedResult =
        new UnassignPrivilegesFromRolesResult.Unassigned(unassignments, timestamp);

    when(unassignPrivilegesFromRolesUseCase.unassignPrivilegesFromRoles(
            any(UnassignPrivilegesFromRolesRequest.class)))
        .thenReturn(unassignedResult);

    UnassignPrivilegesFromRolesServiceRequest request =
        UnassignPrivilegesFromRolesServiceRequest.builder()
            .unassignPrivilegesFromRolesRequest(
                UnassignPrivilegesFromRolesRequest.builder()
                    .roleIdentifiers(b -> b.id("101"))
                    .privilegeIdentifiers(b -> b.id("201").id("202"))
                    .build())
            .build();

    service.unassignPrivilegesFromRoles(request);

    verify(auditLogService).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testUnassignPrivilegesFromRolesDoesNotCreateAuditLogWhenSkipped() {

    UnassignPrivilegesFromRolesResult.Skipped skippedResult =
        new UnassignPrivilegesFromRolesResult.Skipped();

    when(unassignPrivilegesFromRolesUseCase.unassignPrivilegesFromRoles(
            any(UnassignPrivilegesFromRolesRequest.class)))
        .thenReturn(skippedResult);

    UnassignPrivilegesFromRolesServiceRequest request =
        UnassignPrivilegesFromRolesServiceRequest.builder()
            .unassignPrivilegesFromRolesRequest(
                UnassignPrivilegesFromRolesRequest.builder()
                    .roleIdentifiers(b -> b.id("101"))
                    .privilegeIdentifiers(b -> b.id("201"))
                    .build())
            .build();

    service.unassignPrivilegesFromRoles(request);

    verify(auditLogService, never()).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testUnassignPrivilegesFromRolesCreatesBatchParentLogForMultipleRoles() throws Exception {

    RolePrivilegeAssignment unassignments =
        RolePrivilegeAssignment.builder()
            .assignments("101", List.of("201"))
            .assignments("102", List.of("201"))
            .build();
    Instant timestamp = Instant.now();
    UnassignPrivilegesFromRolesResult.Unassigned unassignedResult =
        new UnassignPrivilegesFromRolesResult.Unassigned(unassignments, timestamp);

    when(unassignPrivilegesFromRolesUseCase.unassignPrivilegesFromRoles(
            any(UnassignPrivilegesFromRolesRequest.class)))
        .thenReturn(unassignedResult);

    UnassignPrivilegesFromRolesServiceRequest request =
        UnassignPrivilegesFromRolesServiceRequest.builder()
            .unassignPrivilegesFromRolesRequest(
                UnassignPrivilegesFromRolesRequest.builder()
                    .roleIdentifiers(b -> b.id("101").id("102"))
                    .privilegeIdentifiers(b -> b.id("201"))
                    .build())
            .build();

    service.unassignPrivilegesFromRoles(request);

    verify(auditLogService, times(2)).createLog(any(), any(), any(), any(), any());
  }
}
