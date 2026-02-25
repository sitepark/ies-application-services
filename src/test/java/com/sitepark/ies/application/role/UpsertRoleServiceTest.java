package com.sitepark.ies.application.role;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.label.ReassignLabelsToEntitiesService;
import com.sitepark.ies.application.value.UpsertResult;
import com.sitepark.ies.sharedkernel.patch.PatchDocument;
import com.sitepark.ies.userrepository.core.domain.entity.Role;
import com.sitepark.ies.userrepository.core.domain.value.RoleSnapshot;
import com.sitepark.ies.userrepository.core.usecase.role.CreateRoleResult;
import com.sitepark.ies.userrepository.core.usecase.role.UpdateRoleResult;
import com.sitepark.ies.userrepository.core.usecase.role.UpsertRoleRequest;
import com.sitepark.ies.userrepository.core.usecase.role.UpsertRoleResult;
import com.sitepark.ies.userrepository.core.usecase.role.UpsertRoleUseCase;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UpsertRoleServiceTest {

  private UpsertRoleUseCase upsertRoleUseCase;
  private CreateRoleService createRoleService;
  private UpdateRoleService updateRoleService;

  private ReassignLabelsToEntitiesService reassignLabelsToEntitiesService;

  private UpsertRoleService service;

  @BeforeEach
  void setUp() {
    this.upsertRoleUseCase = mock();
    this.createRoleService = mock();
    this.updateRoleService = mock();
    this.reassignLabelsToEntitiesService = mock();
    this.service =
        new UpsertRoleService(
            upsertRoleUseCase,
            createRoleService,
            updateRoleService,
            reassignLabelsToEntitiesService);
  }

  @Test
  void testUpsertRoleCallsUseCase() {

    Role role = Role.builder().id("101").name("TestRole").build();
    Instant timestamp = Instant.now();
    UpdateRoleResult updateRoleResult =
        new UpdateRoleResult("101", "TestRole", timestamp, null, null, null);
    UpsertRoleResult.Updated updatedResult = new UpsertRoleResult.Updated("101", updateRoleResult);

    when(upsertRoleUseCase.upsertRole(any(UpsertRoleRequest.class))).thenReturn(updatedResult);

    UpsertRoleServiceRequest request =
        UpsertRoleServiceRequest.builder()
            .upsertRoleRequest(UpsertRoleRequest.builder().role(role).build())
            .build();

    service.upsertRole(request);

    verify(upsertRoleUseCase).upsertRole(any(UpsertRoleRequest.class));
  }

  @Test
  void testUpsertRoleReturnsCreatedResultWhenRoleCreated() {

    Role role = Role.builder().id("101").name("TestRole").build();
    RoleSnapshot snapshot = new RoleSnapshot(role, List.of(), List.of());
    Instant timestamp = Instant.now();
    CreateRoleResult createRoleResult = new CreateRoleResult("101", snapshot, null, timestamp);
    UpsertRoleResult.Created createdResult = new UpsertRoleResult.Created("101", createRoleResult);

    when(upsertRoleUseCase.upsertRole(any(UpsertRoleRequest.class))).thenReturn(createdResult);

    UpsertRoleServiceRequest request =
        UpsertRoleServiceRequest.builder()
            .upsertRoleRequest(UpsertRoleRequest.builder().role(role).build())
            .build();

    UpsertResult result = service.upsertRole(request);

    assertInstanceOf(
        UpsertResult.Created.class,
        result,
        "upsertRole() should return Created when role was created");
  }

  @Test
  void testUpsertRoleCallsCreateAuditLogsWhenCreated() {

    Role role = Role.builder().id("101").name("TestRole").build();
    RoleSnapshot snapshot = new RoleSnapshot(role, List.of(), List.of());
    Instant timestamp = Instant.now();
    CreateRoleResult createRoleResult = new CreateRoleResult("101", snapshot, null, timestamp);
    UpsertRoleResult.Created createdResult = new UpsertRoleResult.Created("101", createRoleResult);

    when(upsertRoleUseCase.upsertRole(any(UpsertRoleRequest.class))).thenReturn(createdResult);

    UpsertRoleServiceRequest request =
        UpsertRoleServiceRequest.builder()
            .upsertRoleRequest(UpsertRoleRequest.builder().role(role).build())
            .build();

    service.upsertRole(request);

    verify(createRoleService).createAuditLogs(any(), any());
  }

  @Test
  void testUpsertRoleReturnsUpdatedTrueWhenRoleChanged() {

    Role role = Role.builder().id("101").name("TestRole").build();
    Instant timestamp = Instant.now();
    PatchDocument patchDoc = mock();
    when(patchDoc.isEmpty()).thenReturn(false);
    UpdateRoleResult updateRoleResult =
        new UpdateRoleResult("101", "TestRole", timestamp, patchDoc, patchDoc, null);
    UpsertRoleResult.Updated updatedResult = new UpsertRoleResult.Updated("101", updateRoleResult);

    when(upsertRoleUseCase.upsertRole(any(UpsertRoleRequest.class))).thenReturn(updatedResult);

    UpsertRoleServiceRequest request =
        UpsertRoleServiceRequest.builder()
            .upsertRoleRequest(UpsertRoleRequest.builder().role(role).build())
            .build();

    UpsertResult result = service.upsertRole(request);

    assertEquals(
        UpsertResult.updated(true),
        result,
        "upsertRole() should return Updated(true) when changes were made");
  }

  @Test
  void testUpsertRoleCallsUpdateAuditLogsWhenRoleChanged() {

    Role role = Role.builder().id("101").name("TestRole").build();
    Instant timestamp = Instant.now();
    PatchDocument patchDoc = mock();
    when(patchDoc.isEmpty()).thenReturn(false);
    UpdateRoleResult updateRoleResult =
        new UpdateRoleResult("101", "TestRole", timestamp, patchDoc, patchDoc, null);
    UpsertRoleResult.Updated updatedResult = new UpsertRoleResult.Updated("101", updateRoleResult);

    when(upsertRoleUseCase.upsertRole(any(UpsertRoleRequest.class))).thenReturn(updatedResult);

    UpsertRoleServiceRequest request =
        UpsertRoleServiceRequest.builder()
            .upsertRoleRequest(UpsertRoleRequest.builder().role(role).build())
            .build();

    service.upsertRole(request);

    verify(updateRoleService).createAuditLogs(any(), any());
  }

  @Test
  void testUpsertRoleReturnsUpdatedFalseWhenNoChanges() {

    Role role = Role.builder().id("101").name("TestRole").build();
    Instant timestamp = Instant.now();
    UpdateRoleResult updateRoleResult =
        new UpdateRoleResult("101", "TestRole", timestamp, null, null, null);
    UpsertRoleResult.Updated updatedResult = new UpsertRoleResult.Updated("101", updateRoleResult);

    when(upsertRoleUseCase.upsertRole(any(UpsertRoleRequest.class))).thenReturn(updatedResult);

    UpsertRoleServiceRequest request =
        UpsertRoleServiceRequest.builder()
            .upsertRoleRequest(UpsertRoleRequest.builder().role(role).build())
            .build();

    UpsertResult result = service.upsertRole(request);

    assertEquals(
        UpsertResult.updated(false),
        result,
        "upsertRole() should return Updated(false) when no changes were made");
  }

  @Test
  void testUpsertRoleDoesNotCallUpdateAuditLogsWhenNoChanges() {

    Role role = Role.builder().id("101").name("TestRole").build();
    Instant timestamp = Instant.now();
    UpdateRoleResult updateRoleResult =
        new UpdateRoleResult("101", "TestRole", timestamp, null, null, null);
    UpsertRoleResult.Updated updatedResult = new UpsertRoleResult.Updated("101", updateRoleResult);

    when(upsertRoleUseCase.upsertRole(any(UpsertRoleRequest.class))).thenReturn(updatedResult);

    UpsertRoleServiceRequest request =
        UpsertRoleServiceRequest.builder()
            .upsertRoleRequest(UpsertRoleRequest.builder().role(role).build())
            .build();

    service.upsertRole(request);

    verify(updateRoleService, never()).createAuditLogs(any(), any());
  }

  @Test
  void testUpsertRoleReassignsLabelsWhenLabelIdentifiersProvided() {

    Role role = Role.builder().id("101").name("TestRole").build();
    Instant timestamp = Instant.now();
    UpdateRoleResult updateRoleResult =
        new UpdateRoleResult("101", "TestRole", timestamp, null, null, null);
    UpsertRoleResult.Updated updatedResult = new UpsertRoleResult.Updated("101", updateRoleResult);

    when(upsertRoleUseCase.upsertRole(any(UpsertRoleRequest.class))).thenReturn(updatedResult);

    UpsertRoleServiceRequest request =
        UpsertRoleServiceRequest.builder()
            .upsertRoleRequest(UpsertRoleRequest.builder().role(role).build())
            .labelIdentifiers(b -> b.id("501"))
            .build();

    service.upsertRole(request);

    verify(reassignLabelsToEntitiesService).reassignEntitiesFromLabels(any());
  }

  @Test
  void testUpsertRoleDoesNotReassignLabelsWhenNoLabelIdentifiers() {

    Role role = Role.builder().id("101").name("TestRole").build();
    Instant timestamp = Instant.now();
    UpdateRoleResult updateRoleResult =
        new UpdateRoleResult("101", "TestRole", timestamp, null, null, null);
    UpsertRoleResult.Updated updatedResult = new UpsertRoleResult.Updated("101", updateRoleResult);

    when(upsertRoleUseCase.upsertRole(any(UpsertRoleRequest.class))).thenReturn(updatedResult);

    UpsertRoleServiceRequest request =
        UpsertRoleServiceRequest.builder()
            .upsertRoleRequest(UpsertRoleRequest.builder().role(role).build())
            .build();

    service.upsertRole(request);

    verify(reassignLabelsToEntitiesService, never()).reassignEntitiesFromLabels(any());
  }
}
