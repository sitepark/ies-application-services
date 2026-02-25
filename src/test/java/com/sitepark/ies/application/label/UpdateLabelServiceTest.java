package com.sitepark.ies.application.label;

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
import com.sitepark.ies.label.core.domain.entity.Label;
import com.sitepark.ies.label.core.domain.value.LabelScopeAssignment;
import com.sitepark.ies.label.core.usecase.LabelUpdateResult;
import com.sitepark.ies.label.core.usecase.ReassignScopesToLabelsResult;
import com.sitepark.ies.label.core.usecase.UpdateLabelRequest;
import com.sitepark.ies.label.core.usecase.UpdateLabelResult;
import com.sitepark.ies.label.core.usecase.UpdateLabelUseCase;
import com.sitepark.ies.sharedkernel.patch.PatchDocument;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UpdateLabelServiceTest {

  private UpdateLabelUseCase updateLabelUseCase;

  @SuppressWarnings("PMD.SingularField")
  private MultiEntityNameResolver multiEntityNameResolver;

  @SuppressWarnings("PMD.SingularField")
  private ApplicationAuditLogServiceFactory auditLogServiceFactory;

  private ApplicationAuditLogService auditLogService;
  private UpdateLabelService service;

  @BeforeEach
  void setUp() {
    this.updateLabelUseCase = mock();
    this.multiEntityNameResolver = mock();
    this.auditLogServiceFactory = mock();
    this.auditLogService = mock();
    this.service =
        new UpdateLabelService(updateLabelUseCase, multiEntityNameResolver, auditLogServiceFactory);
    when(auditLogServiceFactory.create(any(), any())).thenReturn(auditLogService);
    when(multiEntityNameResolver.resolveName(any())).thenReturn("TestLabel");
  }

  @Test
  void testUpdateLabelCallsUseCase() {

    Label label = Label.builder().id("401").name("TestLabel").build();
    Instant timestamp = Instant.now();
    UpdateLabelResult result =
        new UpdateLabelResult(
            "401",
            timestamp,
            LabelUpdateResult.unchanged(),
            ReassignScopesToLabelsResult.skipped());

    when(updateLabelUseCase.updateLabel(any(UpdateLabelRequest.class))).thenReturn(result);

    UpdateLabelServiceRequest request =
        UpdateLabelServiceRequest.builder()
            .updateLabelRequest(UpdateLabelRequest.builder().label(label).build())
            .build();

    service.updateLabel(request);

    verify(updateLabelUseCase).updateLabel(any(UpdateLabelRequest.class));
  }

  @Test
  void testUpdateLabelReturnsTrueWhenLabelChanged() {

    Label label = Label.builder().id("401").name("TestLabel").build();
    Instant timestamp = Instant.now();
    PatchDocument patchDoc = mock();
    UpdateLabelResult result =
        new UpdateLabelResult(
            "401",
            timestamp,
            LabelUpdateResult.updated("TestLabel", patchDoc, patchDoc),
            ReassignScopesToLabelsResult.skipped());

    when(updateLabelUseCase.updateLabel(any(UpdateLabelRequest.class))).thenReturn(result);

    UpdateLabelServiceRequest request =
        UpdateLabelServiceRequest.builder()
            .updateLabelRequest(UpdateLabelRequest.builder().label(label).build())
            .build();

    boolean updated = service.updateLabel(request);

    assertEquals(true, updated, "updateLabel() should return true when changes were made");
  }

  @Test
  void testUpdateLabelReturnsFalseWhenNoChanges() {

    Label label = Label.builder().id("401").name("TestLabel").build();
    Instant timestamp = Instant.now();
    UpdateLabelResult result =
        new UpdateLabelResult(
            "401",
            timestamp,
            LabelUpdateResult.unchanged(),
            ReassignScopesToLabelsResult.skipped());

    when(updateLabelUseCase.updateLabel(any(UpdateLabelRequest.class))).thenReturn(result);

    UpdateLabelServiceRequest request =
        UpdateLabelServiceRequest.builder()
            .updateLabelRequest(UpdateLabelRequest.builder().label(label).build())
            .build();

    boolean updated = service.updateLabel(request);

    assertEquals(false, updated, "updateLabel() should return false when no changes were made");
  }

  @Test
  void testUpdateLabelCreatesAuditLogWhenLabelUpdated() throws Exception {

    Label label = Label.builder().id("401").name("TestLabel").build();
    Instant timestamp = Instant.now();
    PatchDocument patchDoc = mock();
    UpdateLabelResult result =
        new UpdateLabelResult(
            "401",
            timestamp,
            LabelUpdateResult.updated("TestLabel", patchDoc, patchDoc),
            ReassignScopesToLabelsResult.skipped());

    when(updateLabelUseCase.updateLabel(any(UpdateLabelRequest.class))).thenReturn(result);

    UpdateLabelServiceRequest request =
        UpdateLabelServiceRequest.builder()
            .updateLabelRequest(UpdateLabelRequest.builder().label(label).build())
            .build();

    service.updateLabel(request);

    verify(auditLogService).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testUpdateLabelDoesNotCreateAuditLogWhenUnchanged() {

    Label label = Label.builder().id("401").name("TestLabel").build();
    Instant timestamp = Instant.now();
    UpdateLabelResult result =
        new UpdateLabelResult(
            "401",
            timestamp,
            LabelUpdateResult.unchanged(),
            ReassignScopesToLabelsResult.skipped());

    when(updateLabelUseCase.updateLabel(any(UpdateLabelRequest.class))).thenReturn(result);

    UpdateLabelServiceRequest request =
        UpdateLabelServiceRequest.builder()
            .updateLabelRequest(UpdateLabelRequest.builder().label(label).build())
            .build();

    service.updateLabel(request);

    verify(auditLogService, never()).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testUpdateLabelCreatesTwoAuditLogsForAssignedAndUnassignedScopes() throws Exception {

    Label label = Label.builder().id("401").name("TestLabel").build();
    Instant timestamp = Instant.now();
    LabelScopeAssignment assignments =
        LabelScopeAssignment.builder().assignments("401", "scope1").build();
    LabelScopeAssignment unassignments =
        LabelScopeAssignment.builder().assignments("401", "scope2").build();
    UpdateLabelResult result =
        new UpdateLabelResult(
            "401",
            timestamp,
            LabelUpdateResult.unchanged(),
            ReassignScopesToLabelsResult.reassigned(assignments, unassignments, timestamp));

    when(updateLabelUseCase.updateLabel(any(UpdateLabelRequest.class))).thenReturn(result);

    UpdateLabelServiceRequest request =
        UpdateLabelServiceRequest.builder()
            .updateLabelRequest(UpdateLabelRequest.builder().label(label).build())
            .build();

    service.updateLabel(request);

    verify(auditLogService, times(2)).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testCreateAuditLogsCreatesAuditLogWhenLabelUpdated() throws Exception {

    Instant timestamp = Instant.now();
    PatchDocument patchDoc = mock();
    UpdateLabelResult result =
        new UpdateLabelResult(
            "401",
            timestamp,
            LabelUpdateResult.updated("TestLabel", patchDoc, patchDoc),
            ReassignScopesToLabelsResult.skipped());

    service.createAuditLogs(result, null);

    verify(auditLogService).createLog(any(), any(), any(), any(), any());
  }
}
