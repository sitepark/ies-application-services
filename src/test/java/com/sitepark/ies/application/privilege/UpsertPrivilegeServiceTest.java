package com.sitepark.ies.application.privilege;

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
import com.sitepark.ies.userrepository.core.domain.entity.Privilege;
import com.sitepark.ies.userrepository.core.domain.value.PrivilegeSnapshot;
import com.sitepark.ies.userrepository.core.usecase.privilege.CreatePrivilegeResult;
import com.sitepark.ies.userrepository.core.usecase.privilege.UpdatePrivilegeResult;
import com.sitepark.ies.userrepository.core.usecase.privilege.UpsertPrivilegeRequest;
import com.sitepark.ies.userrepository.core.usecase.privilege.UpsertPrivilegeResult;
import com.sitepark.ies.userrepository.core.usecase.privilege.UpsertPrivilegeUseCase;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UpsertPrivilegeServiceTest {

  private UpsertPrivilegeUseCase upsertPrivilegeUseCase;
  private CreatePrivilegeService createPrivilegeService;
  private UpdatePrivilegeService updatePrivilegeService;

  private ReassignLabelsToEntitiesService reassignLabelsToEntitiesService;

  private UpsertPrivilegeService service;

  @BeforeEach
  void setUp() {
    this.upsertPrivilegeUseCase = mock();
    this.createPrivilegeService = mock();
    this.updatePrivilegeService = mock();
    this.reassignLabelsToEntitiesService = mock();
    this.service =
        new UpsertPrivilegeService(
            upsertPrivilegeUseCase,
            createPrivilegeService,
            updatePrivilegeService,
            reassignLabelsToEntitiesService);
  }

  @Test
  void testUpsertPrivilegeCallsUseCase() {

    Privilege privilege = Privilege.builder().id("201").name("TestPrivilege").build();
    Instant timestamp = Instant.now();
    UpdatePrivilegeResult updatePrivilegeResult =
        new UpdatePrivilegeResult("201", "TestPrivilege", timestamp, null, null, null);
    UpsertPrivilegeResult.Updated updatedResult =
        new UpsertPrivilegeResult.Updated("201", updatePrivilegeResult);

    when(upsertPrivilegeUseCase.upsertPrivilege(any(UpsertPrivilegeRequest.class)))
        .thenReturn(updatedResult);

    UpsertPrivilegeServiceRequest request =
        UpsertPrivilegeServiceRequest.builder()
            .upsertPrivilegeRequest(UpsertPrivilegeRequest.builder().privilege(privilege).build())
            .build();

    service.upsertPrivilege(request);

    verify(upsertPrivilegeUseCase).upsertPrivilege(any(UpsertPrivilegeRequest.class));
  }

  @Test
  void testUpsertPrivilegeReturnsCreatedResultWhenPrivilegeCreated() {

    Privilege privilege = Privilege.builder().id("201").name("TestPrivilege").build();
    PrivilegeSnapshot snapshot = new PrivilegeSnapshot(privilege, List.of());
    Instant timestamp = Instant.now();
    CreatePrivilegeResult createPrivilegeResult =
        new CreatePrivilegeResult("201", snapshot, null, timestamp);
    UpsertPrivilegeResult.Created createdResult =
        new UpsertPrivilegeResult.Created("201", createPrivilegeResult);

    when(upsertPrivilegeUseCase.upsertPrivilege(any(UpsertPrivilegeRequest.class)))
        .thenReturn(createdResult);

    UpsertPrivilegeServiceRequest request =
        UpsertPrivilegeServiceRequest.builder()
            .upsertPrivilegeRequest(UpsertPrivilegeRequest.builder().privilege(privilege).build())
            .build();

    UpsertResult result = service.upsertPrivilege(request);

    assertInstanceOf(
        UpsertResult.Created.class,
        result,
        "upsertPrivilege() should return Created when privilege was created");
  }

  @Test
  void testUpsertPrivilegeCallsCreateAuditLogsWhenCreated() {

    Privilege privilege = Privilege.builder().id("201").name("TestPrivilege").build();
    PrivilegeSnapshot snapshot = new PrivilegeSnapshot(privilege, List.of());
    Instant timestamp = Instant.now();
    CreatePrivilegeResult createPrivilegeResult =
        new CreatePrivilegeResult("201", snapshot, null, timestamp);
    UpsertPrivilegeResult.Created createdResult =
        new UpsertPrivilegeResult.Created("201", createPrivilegeResult);

    when(upsertPrivilegeUseCase.upsertPrivilege(any(UpsertPrivilegeRequest.class)))
        .thenReturn(createdResult);

    UpsertPrivilegeServiceRequest request =
        UpsertPrivilegeServiceRequest.builder()
            .upsertPrivilegeRequest(UpsertPrivilegeRequest.builder().privilege(privilege).build())
            .build();

    service.upsertPrivilege(request);

    verify(createPrivilegeService).createAuditLogs(any(), any());
  }

  @Test
  void testUpsertPrivilegeReturnsUpdatedTrueWhenPrivilegeChanged() {

    Privilege privilege = Privilege.builder().id("201").name("TestPrivilege").build();
    Instant timestamp = Instant.now();
    PatchDocument patchDoc = mock();
    when(patchDoc.isEmpty()).thenReturn(false);
    UpdatePrivilegeResult updatePrivilegeResult =
        new UpdatePrivilegeResult("201", "TestPrivilege", timestamp, patchDoc, patchDoc, null);
    UpsertPrivilegeResult.Updated updatedResult =
        new UpsertPrivilegeResult.Updated("201", updatePrivilegeResult);

    when(upsertPrivilegeUseCase.upsertPrivilege(any(UpsertPrivilegeRequest.class)))
        .thenReturn(updatedResult);

    UpsertPrivilegeServiceRequest request =
        UpsertPrivilegeServiceRequest.builder()
            .upsertPrivilegeRequest(UpsertPrivilegeRequest.builder().privilege(privilege).build())
            .build();

    UpsertResult result = service.upsertPrivilege(request);

    assertEquals(
        UpsertResult.updated(true),
        result,
        "upsertPrivilege() should return Updated(true) when changes were made");
  }

  @Test
  void testUpsertPrivilegeCallsUpdateAuditLogsWhenPrivilegeChanged() {

    Privilege privilege = Privilege.builder().id("201").name("TestPrivilege").build();
    Instant timestamp = Instant.now();
    PatchDocument patchDoc = mock();
    when(patchDoc.isEmpty()).thenReturn(false);
    UpdatePrivilegeResult updatePrivilegeResult =
        new UpdatePrivilegeResult("201", "TestPrivilege", timestamp, patchDoc, patchDoc, null);
    UpsertPrivilegeResult.Updated updatedResult =
        new UpsertPrivilegeResult.Updated("201", updatePrivilegeResult);

    when(upsertPrivilegeUseCase.upsertPrivilege(any(UpsertPrivilegeRequest.class)))
        .thenReturn(updatedResult);

    UpsertPrivilegeServiceRequest request =
        UpsertPrivilegeServiceRequest.builder()
            .upsertPrivilegeRequest(UpsertPrivilegeRequest.builder().privilege(privilege).build())
            .build();

    service.upsertPrivilege(request);

    verify(updatePrivilegeService).createAuditLogs(any(), any());
  }

  @Test
  void testUpsertPrivilegeReturnsUpdatedFalseWhenNoChanges() {

    Privilege privilege = Privilege.builder().id("201").name("TestPrivilege").build();
    Instant timestamp = Instant.now();
    UpdatePrivilegeResult updatePrivilegeResult =
        new UpdatePrivilegeResult("201", "TestPrivilege", timestamp, null, null, null);
    UpsertPrivilegeResult.Updated updatedResult =
        new UpsertPrivilegeResult.Updated("201", updatePrivilegeResult);

    when(upsertPrivilegeUseCase.upsertPrivilege(any(UpsertPrivilegeRequest.class)))
        .thenReturn(updatedResult);

    UpsertPrivilegeServiceRequest request =
        UpsertPrivilegeServiceRequest.builder()
            .upsertPrivilegeRequest(UpsertPrivilegeRequest.builder().privilege(privilege).build())
            .build();

    UpsertResult result = service.upsertPrivilege(request);

    assertEquals(
        UpsertResult.updated(false),
        result,
        "upsertPrivilege() should return Updated(false) when no changes were made");
  }

  @Test
  void testUpsertPrivilegeDoesNotCallUpdateAuditLogsWhenNoChanges() {

    Privilege privilege = Privilege.builder().id("201").name("TestPrivilege").build();
    Instant timestamp = Instant.now();
    UpdatePrivilegeResult updatePrivilegeResult =
        new UpdatePrivilegeResult("201", "TestPrivilege", timestamp, null, null, null);
    UpsertPrivilegeResult.Updated updatedResult =
        new UpsertPrivilegeResult.Updated("201", updatePrivilegeResult);

    when(upsertPrivilegeUseCase.upsertPrivilege(any(UpsertPrivilegeRequest.class)))
        .thenReturn(updatedResult);

    UpsertPrivilegeServiceRequest request =
        UpsertPrivilegeServiceRequest.builder()
            .upsertPrivilegeRequest(UpsertPrivilegeRequest.builder().privilege(privilege).build())
            .build();

    service.upsertPrivilege(request);

    verify(updatePrivilegeService, never()).createAuditLogs(any(), any());
  }

  @Test
  void testUpsertPrivilegeReassignsLabelsWhenLabelIdentifiersProvided() {

    Privilege privilege = Privilege.builder().id("201").name("TestPrivilege").build();
    Instant timestamp = Instant.now();
    UpdatePrivilegeResult updatePrivilegeResult =
        new UpdatePrivilegeResult("201", "TestPrivilege", timestamp, null, null, null);
    UpsertPrivilegeResult.Updated updatedResult =
        new UpsertPrivilegeResult.Updated("201", updatePrivilegeResult);

    when(upsertPrivilegeUseCase.upsertPrivilege(any(UpsertPrivilegeRequest.class)))
        .thenReturn(updatedResult);

    UpsertPrivilegeServiceRequest request =
        UpsertPrivilegeServiceRequest.builder()
            .upsertPrivilegeRequest(UpsertPrivilegeRequest.builder().privilege(privilege).build())
            .labelIdentifiers(b -> b.id("501"))
            .build();

    service.upsertPrivilege(request);

    verify(reassignLabelsToEntitiesService).reassignEntitiesFromLabels(any());
  }

  @Test
  void testUpsertPrivilegeDoesNotReassignLabelsWhenNoLabelIdentifiers() {

    Privilege privilege = Privilege.builder().id("201").name("TestPrivilege").build();
    Instant timestamp = Instant.now();
    UpdatePrivilegeResult updatePrivilegeResult =
        new UpdatePrivilegeResult("201", "TestPrivilege", timestamp, null, null, null);
    UpsertPrivilegeResult.Updated updatedResult =
        new UpsertPrivilegeResult.Updated("201", updatePrivilegeResult);

    when(upsertPrivilegeUseCase.upsertPrivilege(any(UpsertPrivilegeRequest.class)))
        .thenReturn(updatedResult);

    UpsertPrivilegeServiceRequest request =
        UpsertPrivilegeServiceRequest.builder()
            .upsertPrivilegeRequest(UpsertPrivilegeRequest.builder().privilege(privilege).build())
            .build();

    service.upsertPrivilege(request);

    verify(reassignLabelsToEntitiesService, never()).reassignEntitiesFromLabels(any());
  }
}
