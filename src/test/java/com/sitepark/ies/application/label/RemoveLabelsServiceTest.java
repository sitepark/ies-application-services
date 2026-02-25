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
import com.sitepark.ies.label.core.domain.entity.Label;
import com.sitepark.ies.label.core.domain.value.LabelSnapshot;
import com.sitepark.ies.label.core.usecase.RemoveLabelRequest;
import com.sitepark.ies.label.core.usecase.RemoveLabelResult;
import com.sitepark.ies.label.core.usecase.RemoveLabelUseCase;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RemoveLabelsServiceTest {

  private RemoveLabelUseCase removeLabelUseCase;

  @SuppressWarnings("PMD.SingularField")
  private ApplicationAuditLogServiceFactory auditLogServiceFactory;

  private ApplicationAuditLogService auditLogService;
  private Clock clock;
  private RemoveLabelsService service;

  @BeforeEach
  void setUp() {
    this.removeLabelUseCase = mock();
    this.auditLogServiceFactory = mock();
    this.auditLogService = mock();
    this.clock = mock();
    this.service = new RemoveLabelsService(removeLabelUseCase, auditLogServiceFactory, clock);
    when(auditLogServiceFactory.create(any(), any())).thenReturn(auditLogService);
  }

  @Test
  void testRemoveLabelsReturnsZeroWhenNoIdentifiers() {

    RemoveLabelsServiceRequest request = RemoveLabelsServiceRequest.builder().build();

    int result = service.removeLabels(request);

    assertEquals(0, result, "removeLabels() should return 0 when no identifiers are provided");
  }

  @Test
  void testRemoveLabelsCallsUseCase() {

    Label label = Label.builder().id("401").name("TestLabel").build();
    LabelSnapshot snapshot = new LabelSnapshot(label, List.of(), List.of());
    Instant timestamp = Instant.now();
    RemoveLabelResult.Removed removedResult =
        new RemoveLabelResult.Removed("401", "TestLabel", snapshot, timestamp);

    when(removeLabelUseCase.removeLabel(any(RemoveLabelRequest.class))).thenReturn(removedResult);
    when(clock.instant()).thenReturn(timestamp);
    when(clock.getZone()).thenReturn(ZoneId.systemDefault());

    RemoveLabelsServiceRequest request =
        RemoveLabelsServiceRequest.builder().identifiers(b -> b.id("401")).build();

    service.removeLabels(request);

    verify(removeLabelUseCase).removeLabel(any(RemoveLabelRequest.class));
  }

  @Test
  void testRemoveLabelsReturnsRemovedCount() {

    Label label = Label.builder().id("401").name("TestLabel").build();
    LabelSnapshot snapshot = new LabelSnapshot(label, List.of(), List.of());
    Instant timestamp = Instant.now();
    RemoveLabelResult.Removed removedResult =
        new RemoveLabelResult.Removed("401", "TestLabel", snapshot, timestamp);

    when(removeLabelUseCase.removeLabel(any(RemoveLabelRequest.class))).thenReturn(removedResult);
    when(clock.instant()).thenReturn(timestamp);
    when(clock.getZone()).thenReturn(ZoneId.systemDefault());

    RemoveLabelsServiceRequest request =
        RemoveLabelsServiceRequest.builder().identifiers(b -> b.id("401")).build();

    int result = service.removeLabels(request);

    assertEquals(1, result, "removeLabels() should return the count of removed labels");
  }

  @Test
  void testRemoveLabelsCreatesAuditLogWhenLabelWasRemoved() throws Exception {

    Label label = Label.builder().id("401").name("TestLabel").build();
    LabelSnapshot snapshot = new LabelSnapshot(label, List.of(), List.of());
    Instant timestamp = Instant.now();
    RemoveLabelResult.Removed removedResult =
        new RemoveLabelResult.Removed("401", "TestLabel", snapshot, timestamp);

    when(removeLabelUseCase.removeLabel(any(RemoveLabelRequest.class))).thenReturn(removedResult);
    when(clock.instant()).thenReturn(timestamp);
    when(clock.getZone()).thenReturn(ZoneId.systemDefault());

    RemoveLabelsServiceRequest request =
        RemoveLabelsServiceRequest.builder().identifiers(b -> b.id("401")).build();

    service.removeLabels(request);

    verify(auditLogService).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testRemoveLabelsDoesNotCreateAuditLogWhenLabelWasSkipped() {

    RemoveLabelResult.Skipped skippedResult =
        new RemoveLabelResult.Skipped("401", "Built-in label cannot be removed");

    when(removeLabelUseCase.removeLabel(any(RemoveLabelRequest.class))).thenReturn(skippedResult);

    RemoveLabelsServiceRequest request =
        RemoveLabelsServiceRequest.builder().identifiers(b -> b.id("401")).build();

    service.removeLabels(request);

    verify(auditLogService, never()).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testRemoveLabelsCreatesBatchParentLogForMultipleLabels() throws Exception {

    Label label1 = Label.builder().id("401").name("TestLabel1").build();
    Label label2 = Label.builder().id("402").name("TestLabel2").build();
    LabelSnapshot snapshot1 = new LabelSnapshot(label1, List.of(), List.of());
    LabelSnapshot snapshot2 = new LabelSnapshot(label2, List.of(), List.of());
    Instant timestamp = Instant.now();

    RemoveLabelResult.Removed removedResult1 =
        new RemoveLabelResult.Removed("401", "TestLabel1", snapshot1, timestamp);
    RemoveLabelResult.Removed removedResult2 =
        new RemoveLabelResult.Removed("402", "TestLabel2", snapshot2, timestamp);

    when(removeLabelUseCase.removeLabel(any(RemoveLabelRequest.class)))
        .thenReturn(removedResult1, removedResult2);
    when(clock.instant()).thenReturn(timestamp);
    when(clock.getZone()).thenReturn(ZoneId.systemDefault());
    when(auditLogService.createBatchLog(any(), any()))
        .thenReturn("batch-parent-id", "audit-1", "audit-2");

    RemoveLabelsServiceRequest request =
        RemoveLabelsServiceRequest.builder().identifiers(b -> b.id("401").id("402")).build();

    service.removeLabels(request);

    verify(auditLogService, times(2)).createLog(any(), any(), any(), any(), any());
  }
}
