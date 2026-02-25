package com.sitepark.ies.application.role;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.label.ReassignLabelsToEntitiesService;
import com.sitepark.ies.userrepository.core.domain.entity.Role;
import com.sitepark.ies.userrepository.core.domain.value.RolePrivilegeAssignment;
import com.sitepark.ies.userrepository.core.domain.value.RoleSnapshot;
import com.sitepark.ies.userrepository.core.usecase.role.AssignPrivilegesToRolesResult;
import com.sitepark.ies.userrepository.core.usecase.role.CreateRoleResult;
import com.sitepark.ies.userrepository.core.usecase.role.CreateRoleUseCase;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreateRoleServiceTest {

  private CreateRoleUseCase createRoleUseCase;

  private ReassignLabelsToEntitiesService reassignLabelsToEntitiesService;

  @SuppressWarnings("PMD.SingularField")
  private ApplicationAuditLogServiceFactory auditLogServiceFactory;

  private ApplicationAuditLogService auditLogService;
  private CreateRoleService service;

  @BeforeEach
  void setUp() {
    this.createRoleUseCase = mock();
    this.reassignLabelsToEntitiesService = mock();
    this.auditLogServiceFactory = mock();
    this.auditLogService = mock();
    this.service =
        new CreateRoleService(
            createRoleUseCase, reassignLabelsToEntitiesService, auditLogServiceFactory);
    when(auditLogServiceFactory.create(any(), any())).thenReturn(auditLogService);
  }

  @Test
  void testCreateRoleReturnsRoleId() {

    Role role = Role.builder().id("123").name("TestRole").build();
    RoleSnapshot snapshot = new RoleSnapshot(role, List.of(), List.of());
    Instant timestamp = Instant.now();
    CreateRoleResult result = new CreateRoleResult("123", snapshot, null, timestamp);

    when(createRoleUseCase.createRole(
            any(com.sitepark.ies.userrepository.core.usecase.role.CreateRoleRequest.class)))
        .thenReturn(result);

    CreateRoleServiceRequest request =
        CreateRoleServiceRequest.builder()
            .createRoleRequest(
                com.sitepark.ies.userrepository.core.usecase.role.CreateRoleRequest.builder()
                    .role(role)
                    .build())
            .build();

    String roleId = service.createRole(request);

    assertEquals("123", roleId, "Should return the created role ID");
  }

  @Test
  void testCreateRoleCallsUseCase() {

    Role role = Role.builder().id("123").name("TestRole").build();
    RoleSnapshot snapshot = new RoleSnapshot(role, List.of(), List.of());
    Instant timestamp = Instant.now();
    CreateRoleResult result = new CreateRoleResult("123", snapshot, null, timestamp);

    when(createRoleUseCase.createRole(
            any(com.sitepark.ies.userrepository.core.usecase.role.CreateRoleRequest.class)))
        .thenReturn(result);

    CreateRoleServiceRequest request =
        CreateRoleServiceRequest.builder()
            .createRoleRequest(
                com.sitepark.ies.userrepository.core.usecase.role.CreateRoleRequest.builder()
                    .role(role)
                    .build())
            .build();

    service.createRole(request);

    verify(createRoleUseCase)
        .createRole(any(com.sitepark.ies.userrepository.core.usecase.role.CreateRoleRequest.class));
  }

  @Test
  void testCreateRoleCreatesAuditLogForRoleCreation() throws Exception {

    Role role = Role.builder().id("123").name("TestRole").build();
    RoleSnapshot snapshot = new RoleSnapshot(role, List.of(), List.of());
    Instant timestamp = Instant.now();
    CreateRoleResult result = new CreateRoleResult("123", snapshot, null, timestamp);

    when(createRoleUseCase.createRole(
            any(com.sitepark.ies.userrepository.core.usecase.role.CreateRoleRequest.class)))
        .thenReturn(result);

    CreateRoleServiceRequest request =
        CreateRoleServiceRequest.builder()
            .createRoleRequest(
                com.sitepark.ies.userrepository.core.usecase.role.CreateRoleRequest.builder()
                    .role(role)
                    .build())
            .build();

    service.createRole(request);

    verify(auditLogService).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testCreateRoleCreatesAuditLogsForPrivilegeAssignments() throws Exception {

    Role role = Role.builder().id("123").name("TestRole").build();
    RoleSnapshot snapshot = new RoleSnapshot(role, List.of(), List.of("201", "202"));
    RolePrivilegeAssignment assignments =
        RolePrivilegeAssignment.builder().assignments("123", List.of("201", "202")).build();
    Instant timestamp = Instant.now();
    AssignPrivilegesToRolesResult.Assigned privilegeResult =
        new AssignPrivilegesToRolesResult.Assigned(assignments, timestamp);
    CreateRoleResult result = new CreateRoleResult("123", snapshot, privilegeResult, timestamp);

    when(createRoleUseCase.createRole(
            any(com.sitepark.ies.userrepository.core.usecase.role.CreateRoleRequest.class)))
        .thenReturn(result);

    CreateRoleServiceRequest request =
        CreateRoleServiceRequest.builder()
            .createRoleRequest(
                com.sitepark.ies.userrepository.core.usecase.role.CreateRoleRequest.builder()
                    .role(role)
                    .privilegeIdentifiers(b -> b.id("201").id("202"))
                    .build())
            .build();

    service.createRole(request);

    verify(auditLogService, times(2)).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testCreateRoleDoesNotCreatePrivilegeAuditLogWhenNoPrivileges() throws Exception {

    Role role = Role.builder().id("123").name("TestRole").build();
    RoleSnapshot snapshot = new RoleSnapshot(role, List.of(), List.of());
    Instant timestamp = Instant.now();
    CreateRoleResult result = new CreateRoleResult("123", snapshot, null, timestamp);

    when(createRoleUseCase.createRole(
            any(com.sitepark.ies.userrepository.core.usecase.role.CreateRoleRequest.class)))
        .thenReturn(result);

    CreateRoleServiceRequest request =
        CreateRoleServiceRequest.builder()
            .createRoleRequest(
                com.sitepark.ies.userrepository.core.usecase.role.CreateRoleRequest.builder()
                    .role(role)
                    .build())
            .build();

    service.createRole(request);

    verify(auditLogService, times(1)).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testCreateRoleReassignsLabelsWhenLabelIdentifiersProvided() {

    Role role = Role.builder().id("123").name("TestRole").build();
    RoleSnapshot snapshot = new RoleSnapshot(role, List.of(), List.of());
    Instant timestamp = Instant.now();
    CreateRoleResult result = new CreateRoleResult("123", snapshot, null, timestamp);

    when(createRoleUseCase.createRole(
            any(com.sitepark.ies.userrepository.core.usecase.role.CreateRoleRequest.class)))
        .thenReturn(result);

    CreateRoleServiceRequest request =
        CreateRoleServiceRequest.builder()
            .createRoleRequest(
                com.sitepark.ies.userrepository.core.usecase.role.CreateRoleRequest.builder()
                    .role(role)
                    .build())
            .labelIdentifiers(b -> b.id("501"))
            .build();

    service.createRole(request);

    verify(reassignLabelsToEntitiesService).reassignEntitiesFromLabels(any());
  }
}
