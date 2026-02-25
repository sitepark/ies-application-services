package com.sitepark.ies.application.label;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.value.UpsertResult;
import com.sitepark.ies.label.core.domain.entity.Label;
import com.sitepark.ies.label.core.domain.value.LabelSnapshot;
import com.sitepark.ies.label.core.usecase.AssignScopesToLabelsResult;
import com.sitepark.ies.label.core.usecase.CreateLabelResult;
import com.sitepark.ies.label.core.usecase.LabelUpdateResult;
import com.sitepark.ies.label.core.usecase.ReassignScopesToLabelsResult;
import com.sitepark.ies.label.core.usecase.UpdateLabelResult;
import com.sitepark.ies.label.core.usecase.UpsertLabelRequest;
import com.sitepark.ies.label.core.usecase.UpsertLabelResult;
import com.sitepark.ies.label.core.usecase.UpsertLabelUseCase;
import com.sitepark.ies.sharedkernel.patch.PatchDocument;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UpsertLabelServiceTest {

  private UpsertLabelUseCase upsertLabelUseCase;
  private CreateLabelService createLabelService;
  private UpdateLabelService updateLabelService;
  private UpsertLabelService service;

  @BeforeEach
  void setUp() {
    this.upsertLabelUseCase = mock();
    this.createLabelService = mock();
    this.updateLabelService = mock();
    this.service =
        new UpsertLabelService(upsertLabelUseCase, createLabelService, updateLabelService);
  }

  @Test
  void testUpsertLabelCallsUseCase() {

    Label label = Label.builder().id("401").name("TestLabel").build();
    Instant timestamp = Instant.now();
    UpdateLabelResult updateLabelResult =
        new UpdateLabelResult(
            "401",
            timestamp,
            LabelUpdateResult.unchanged(),
            ReassignScopesToLabelsResult.skipped());
    UpsertLabelResult.Updated updatedResult =
        new UpsertLabelResult.Updated("401", updateLabelResult);

    when(upsertLabelUseCase.upsertLabel(any(UpsertLabelRequest.class))).thenReturn(updatedResult);

    UpsertLabelServiceRequest request =
        UpsertLabelServiceRequest.builder()
            .upsertLabelRequest(UpsertLabelRequest.builder().label(label).build())
            .build();

    service.upsertLabel(request);

    verify(upsertLabelUseCase).upsertLabel(any(UpsertLabelRequest.class));
  }

  @Test
  void testUpsertLabelReturnsCreatedResultWhenLabelCreated() {

    Label label = Label.builder().id("401").name("TestLabel").build();
    LabelSnapshot snapshot = new LabelSnapshot(label, List.of(), List.of());
    Instant timestamp = Instant.now();
    CreateLabelResult createLabelResult =
        new CreateLabelResult("401", snapshot, new AssignScopesToLabelsResult.Skipped(), timestamp);
    UpsertLabelResult.Created createdResult =
        new UpsertLabelResult.Created("401", createLabelResult);

    when(upsertLabelUseCase.upsertLabel(any(UpsertLabelRequest.class))).thenReturn(createdResult);

    UpsertLabelServiceRequest request =
        UpsertLabelServiceRequest.builder()
            .upsertLabelRequest(UpsertLabelRequest.builder().label(label).build())
            .build();

    UpsertResult result = service.upsertLabel(request);

    assertInstanceOf(
        UpsertResult.Created.class, result, "upsertLabel() should return Created when created");
  }

  @Test
  void testUpsertLabelCallsCreateAuditLogsWhenCreated() {

    Label label = Label.builder().id("401").name("TestLabel").build();
    LabelSnapshot snapshot = new LabelSnapshot(label, List.of(), List.of());
    Instant timestamp = Instant.now();
    CreateLabelResult createLabelResult =
        new CreateLabelResult("401", snapshot, new AssignScopesToLabelsResult.Skipped(), timestamp);
    UpsertLabelResult.Created createdResult =
        new UpsertLabelResult.Created("401", createLabelResult);

    when(upsertLabelUseCase.upsertLabel(any(UpsertLabelRequest.class))).thenReturn(createdResult);

    UpsertLabelServiceRequest request =
        UpsertLabelServiceRequest.builder()
            .upsertLabelRequest(UpsertLabelRequest.builder().label(label).build())
            .build();

    service.upsertLabel(request);

    verify(createLabelService).createAuditLogs(any(), any());
  }

  @Test
  void testUpsertLabelReturnsUpdatedTrueWhenLabelChanged() {

    Label label = Label.builder().id("401").name("TestLabel").build();
    Instant timestamp = Instant.now();
    PatchDocument patchDoc = mock();
    UpdateLabelResult updateLabelResult =
        new UpdateLabelResult(
            "401",
            timestamp,
            LabelUpdateResult.updated("TestLabel", patchDoc, patchDoc),
            ReassignScopesToLabelsResult.skipped());
    UpsertLabelResult.Updated updatedResult =
        new UpsertLabelResult.Updated("401", updateLabelResult);

    when(upsertLabelUseCase.upsertLabel(any(UpsertLabelRequest.class))).thenReturn(updatedResult);

    UpsertLabelServiceRequest request =
        UpsertLabelServiceRequest.builder()
            .upsertLabelRequest(UpsertLabelRequest.builder().label(label).build())
            .build();

    UpsertResult result = service.upsertLabel(request);

    assertEquals(
        UpsertResult.updated(true),
        result,
        "upsertLabel() should return Updated(true) when changes were made");
  }

  @Test
  void testUpsertLabelCallsUpdateAuditLogsWhenLabelChanged() {

    Label label = Label.builder().id("401").name("TestLabel").build();
    Instant timestamp = Instant.now();
    PatchDocument patchDoc = mock();
    UpdateLabelResult updateLabelResult =
        new UpdateLabelResult(
            "401",
            timestamp,
            LabelUpdateResult.updated("TestLabel", patchDoc, patchDoc),
            ReassignScopesToLabelsResult.skipped());
    UpsertLabelResult.Updated updatedResult =
        new UpsertLabelResult.Updated("401", updateLabelResult);

    when(upsertLabelUseCase.upsertLabel(any(UpsertLabelRequest.class))).thenReturn(updatedResult);

    UpsertLabelServiceRequest request =
        UpsertLabelServiceRequest.builder()
            .upsertLabelRequest(UpsertLabelRequest.builder().label(label).build())
            .build();

    service.upsertLabel(request);

    verify(updateLabelService).createAuditLogs(any(), any());
  }

  @Test
  void testUpsertLabelReturnsUpdatedFalseWhenNoChanges() {

    Label label = Label.builder().id("401").name("TestLabel").build();
    Instant timestamp = Instant.now();
    UpdateLabelResult updateLabelResult =
        new UpdateLabelResult(
            "401",
            timestamp,
            LabelUpdateResult.unchanged(),
            ReassignScopesToLabelsResult.skipped());
    UpsertLabelResult.Updated updatedResult =
        new UpsertLabelResult.Updated("401", updateLabelResult);

    when(upsertLabelUseCase.upsertLabel(any(UpsertLabelRequest.class))).thenReturn(updatedResult);

    UpsertLabelServiceRequest request =
        UpsertLabelServiceRequest.builder()
            .upsertLabelRequest(UpsertLabelRequest.builder().label(label).build())
            .build();

    UpsertResult result = service.upsertLabel(request);

    assertEquals(
        UpsertResult.updated(false),
        result,
        "upsertLabel() should return Updated(false) when no changes were made");
  }

  @Test
  void testUpsertLabelDoesNotCallUpdateAuditLogsWhenNoChanges() {

    Label label = Label.builder().id("401").name("TestLabel").build();
    Instant timestamp = Instant.now();
    UpdateLabelResult updateLabelResult =
        new UpdateLabelResult(
            "401",
            timestamp,
            LabelUpdateResult.unchanged(),
            ReassignScopesToLabelsResult.skipped());
    UpsertLabelResult.Updated updatedResult =
        new UpsertLabelResult.Updated("401", updateLabelResult);

    when(upsertLabelUseCase.upsertLabel(any(UpsertLabelRequest.class))).thenReturn(updatedResult);

    UpsertLabelServiceRequest request =
        UpsertLabelServiceRequest.builder()
            .upsertLabelRequest(UpsertLabelRequest.builder().label(label).build())
            .build();

    service.upsertLabel(request);

    verify(updateLabelService, never()).createAuditLogs(any(), any());
  }
}
