package com.sitepark.ies.application.privilege;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.label.ReassignLabelsToEntitiesService;
import com.sitepark.ies.userrepository.core.domain.entity.Privilege;
import com.sitepark.ies.userrepository.core.domain.value.PrivilegeSnapshot;
import com.sitepark.ies.userrepository.core.domain.value.RolePrivilegeAssignment;
import com.sitepark.ies.userrepository.core.usecase.privilege.CreatePrivilegeResult;
import com.sitepark.ies.userrepository.core.usecase.privilege.CreatePrivilegeUseCase;
import com.sitepark.ies.userrepository.core.usecase.role.AssignPrivilegesToRolesResult;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreatePrivilegeServiceTest {

  private CreatePrivilegeUseCase createPrivilegeUseCase;

  private ReassignLabelsToEntitiesService reassignLabelsToEntitiesService;

  @SuppressWarnings("PMD.SingularField")
  private ApplicationAuditLogServiceFactory auditLogServiceFactory;

  private ApplicationAuditLogService auditLogService;
  private CreatePrivilegeService service;

  @BeforeEach
  void setUp() {
    this.createPrivilegeUseCase = mock();
    this.reassignLabelsToEntitiesService = mock();
    this.auditLogServiceFactory = mock();
    this.auditLogService = mock();
    this.service =
        new CreatePrivilegeService(
            createPrivilegeUseCase, reassignLabelsToEntitiesService, auditLogServiceFactory);
    when(auditLogServiceFactory.create(any(), any())).thenReturn(auditLogService);
  }

  @Test
  void testCreatePrivilegeReturnsPrivilegeId() {

    Privilege privilege = Privilege.builder().id("201").name("TestPrivilege").build();
    PrivilegeSnapshot snapshot = new PrivilegeSnapshot(privilege, List.of());
    Instant timestamp = Instant.now();
    CreatePrivilegeResult result = new CreatePrivilegeResult("201", snapshot, null, timestamp);

    when(createPrivilegeUseCase.createPrivilege(
            any(
                com.sitepark.ies.userrepository.core.usecase.privilege.CreatePrivilegeRequest
                    .class)))
        .thenReturn(result);

    CreatePrivilegeServiceRequest request =
        CreatePrivilegeServiceRequest.builder()
            .createPrivilegeRequest(
                com.sitepark.ies.userrepository.core.usecase.privilege.CreatePrivilegeRequest
                    .builder()
                    .privilege(privilege)
                    .build())
            .build();

    String privilegeId = service.createPrivilege(request);

    assertEquals("201", privilegeId, "Should return the created privilege ID");
  }

  @Test
  void testCreatePrivilegeCallsUseCase() {

    Privilege privilege = Privilege.builder().id("201").name("TestPrivilege").build();
    PrivilegeSnapshot snapshot = new PrivilegeSnapshot(privilege, List.of());
    Instant timestamp = Instant.now();
    CreatePrivilegeResult result = new CreatePrivilegeResult("201", snapshot, null, timestamp);

    when(createPrivilegeUseCase.createPrivilege(
            any(
                com.sitepark.ies.userrepository.core.usecase.privilege.CreatePrivilegeRequest
                    .class)))
        .thenReturn(result);

    CreatePrivilegeServiceRequest request =
        CreatePrivilegeServiceRequest.builder()
            .createPrivilegeRequest(
                com.sitepark.ies.userrepository.core.usecase.privilege.CreatePrivilegeRequest
                    .builder()
                    .privilege(privilege)
                    .build())
            .build();

    service.createPrivilege(request);

    verify(createPrivilegeUseCase)
        .createPrivilege(
            any(
                com.sitepark.ies.userrepository.core.usecase.privilege.CreatePrivilegeRequest
                    .class));
  }

  @Test
  void testCreatePrivilegeCreatesAuditLogForPrivilegeCreation() throws Exception {

    Privilege privilege = Privilege.builder().id("201").name("TestPrivilege").build();
    PrivilegeSnapshot snapshot = new PrivilegeSnapshot(privilege, List.of());
    Instant timestamp = Instant.now();
    CreatePrivilegeResult result = new CreatePrivilegeResult("201", snapshot, null, timestamp);

    when(createPrivilegeUseCase.createPrivilege(
            any(
                com.sitepark.ies.userrepository.core.usecase.privilege.CreatePrivilegeRequest
                    .class)))
        .thenReturn(result);

    CreatePrivilegeServiceRequest request =
        CreatePrivilegeServiceRequest.builder()
            .createPrivilegeRequest(
                com.sitepark.ies.userrepository.core.usecase.privilege.CreatePrivilegeRequest
                    .builder()
                    .privilege(privilege)
                    .build())
            .build();

    service.createPrivilege(request);

    verify(auditLogService).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testCreatePrivilegeCreatesAuditLogsForRoleAssignments() throws Exception {

    Privilege privilege = Privilege.builder().id("201").name("TestPrivilege").build();
    PrivilegeSnapshot snapshot = new PrivilegeSnapshot(privilege, List.of("101", "102"));
    RolePrivilegeAssignment assignments =
        RolePrivilegeAssignment.builder().assignments("101", List.of("201")).build();
    Instant timestamp = Instant.now();
    AssignPrivilegesToRolesResult.Assigned roleResult =
        new AssignPrivilegesToRolesResult.Assigned(assignments, timestamp);
    CreatePrivilegeResult result =
        new CreatePrivilegeResult("201", snapshot, roleResult, timestamp);

    when(createPrivilegeUseCase.createPrivilege(
            any(
                com.sitepark.ies.userrepository.core.usecase.privilege.CreatePrivilegeRequest
                    .class)))
        .thenReturn(result);

    CreatePrivilegeServiceRequest request =
        CreatePrivilegeServiceRequest.builder()
            .createPrivilegeRequest(
                com.sitepark.ies.userrepository.core.usecase.privilege.CreatePrivilegeRequest
                    .builder()
                    .privilege(privilege)
                    .roleIdentifiers(b -> b.id("101"))
                    .build())
            .build();

    service.createPrivilege(request);

    verify(auditLogService, times(2)).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testCreatePrivilegeDoesNotCreateRoleAuditLogWhenNoRoles() throws Exception {

    Privilege privilege = Privilege.builder().id("201").name("TestPrivilege").build();
    PrivilegeSnapshot snapshot = new PrivilegeSnapshot(privilege, List.of());
    Instant timestamp = Instant.now();
    CreatePrivilegeResult result = new CreatePrivilegeResult("201", snapshot, null, timestamp);

    when(createPrivilegeUseCase.createPrivilege(
            any(
                com.sitepark.ies.userrepository.core.usecase.privilege.CreatePrivilegeRequest
                    .class)))
        .thenReturn(result);

    CreatePrivilegeServiceRequest request =
        CreatePrivilegeServiceRequest.builder()
            .createPrivilegeRequest(
                com.sitepark.ies.userrepository.core.usecase.privilege.CreatePrivilegeRequest
                    .builder()
                    .privilege(privilege)
                    .build())
            .build();

    service.createPrivilege(request);

    verify(auditLogService, times(1)).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testCreatePrivilegeReassignsLabelsWhenLabelIdentifiersProvided() {

    Privilege privilege = Privilege.builder().id("201").name("TestPrivilege").build();
    PrivilegeSnapshot snapshot = new PrivilegeSnapshot(privilege, List.of());
    Instant timestamp = Instant.now();
    CreatePrivilegeResult result = new CreatePrivilegeResult("201", snapshot, null, timestamp);

    when(createPrivilegeUseCase.createPrivilege(
            any(
                com.sitepark.ies.userrepository.core.usecase.privilege.CreatePrivilegeRequest
                    .class)))
        .thenReturn(result);

    CreatePrivilegeServiceRequest request =
        CreatePrivilegeServiceRequest.builder()
            .createPrivilegeRequest(
                com.sitepark.ies.userrepository.core.usecase.privilege.CreatePrivilegeRequest
                    .builder()
                    .privilege(privilege)
                    .build())
            .labelIdentifiers(b -> b.id("501"))
            .build();

    service.createPrivilege(request);

    verify(reassignLabelsToEntitiesService).reassignEntitiesFromLabels(any());
  }
}
