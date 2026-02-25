package com.sitepark.ies.application.label;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.label.core.domain.entity.Label;
import com.sitepark.ies.label.core.domain.value.LabelScopeAssignment;
import com.sitepark.ies.label.core.domain.value.LabelSnapshot;
import com.sitepark.ies.label.core.usecase.AssignScopesToLabelsResult;
import com.sitepark.ies.label.core.usecase.CreateLabelRequest;
import com.sitepark.ies.label.core.usecase.CreateLabelResult;
import com.sitepark.ies.label.core.usecase.CreateLabelUseCase;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreateLabelServiceTest {

  private CreateLabelUseCase createLabelUseCase;

  @SuppressWarnings("PMD.SingularField")
  private ApplicationAuditLogServiceFactory auditLogServiceFactory;

  private ApplicationAuditLogService auditLogService;
  private CreateLabelService service;

  @BeforeEach
  void setUp() {
    this.createLabelUseCase = mock();
    this.auditLogServiceFactory = mock();
    this.auditLogService = mock();
    this.service = new CreateLabelService(createLabelUseCase, auditLogServiceFactory);
    when(auditLogServiceFactory.create(any(), any())).thenReturn(auditLogService);
  }

  @Test
  void testCreateLabelCallsUseCase() {

    Label label = Label.builder().id("401").name("TestLabel").build();
    LabelSnapshot snapshot = new LabelSnapshot(label, List.of(), List.of());
    Instant timestamp = Instant.now();
    CreateLabelResult result =
        new CreateLabelResult("401", snapshot, new AssignScopesToLabelsResult.Skipped(), timestamp);

    when(createLabelUseCase.createLabel(any(CreateLabelRequest.class))).thenReturn(result);

    CreateLabelServiceRequest request =
        CreateLabelServiceRequest.builder()
            .createLabelRequest(CreateLabelRequest.builder().label(label).build())
            .build();

    service.createLabel(request);

    verify(createLabelUseCase).createLabel(any(CreateLabelRequest.class));
  }

  @Test
  void testCreateLabelReturnsLabelId() {

    Label label = Label.builder().id("401").name("TestLabel").build();
    LabelSnapshot snapshot = new LabelSnapshot(label, List.of(), List.of());
    Instant timestamp = Instant.now();
    CreateLabelResult result =
        new CreateLabelResult("401", snapshot, new AssignScopesToLabelsResult.Skipped(), timestamp);

    when(createLabelUseCase.createLabel(any(CreateLabelRequest.class))).thenReturn(result);

    CreateLabelServiceRequest request =
        CreateLabelServiceRequest.builder()
            .createLabelRequest(CreateLabelRequest.builder().label(label).build())
            .build();

    String labelId = service.createLabel(request);

    assertEquals("401", labelId, "createLabel() should return the created label ID");
  }

  @Test
  void testCreateLabelCreatesAuditLogForLabelCreation() throws Exception {

    Label label = Label.builder().id("401").name("TestLabel").build();
    LabelSnapshot snapshot = new LabelSnapshot(label, List.of(), List.of());
    Instant timestamp = Instant.now();
    CreateLabelResult result =
        new CreateLabelResult("401", snapshot, new AssignScopesToLabelsResult.Skipped(), timestamp);

    when(createLabelUseCase.createLabel(any(CreateLabelRequest.class))).thenReturn(result);

    CreateLabelServiceRequest request =
        CreateLabelServiceRequest.builder()
            .createLabelRequest(CreateLabelRequest.builder().label(label).build())
            .build();

    service.createLabel(request);

    verify(auditLogService).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testCreateLabelCreatesTwoAuditLogsWhenScopesAreAssigned() throws Exception {

    Label label = Label.builder().id("401").name("TestLabel").build();
    LabelSnapshot snapshot = new LabelSnapshot(label, List.of(), List.of());
    Instant timestamp = Instant.now();
    LabelScopeAssignment assignments =
        LabelScopeAssignment.builder().assignments("401", List.of("scope1")).build();
    AssignScopesToLabelsResult.Assigned assigned =
        new AssignScopesToLabelsResult.Assigned(assignments, timestamp);
    CreateLabelResult result = new CreateLabelResult("401", snapshot, assigned, timestamp);

    when(createLabelUseCase.createLabel(any(CreateLabelRequest.class))).thenReturn(result);

    CreateLabelServiceRequest request =
        CreateLabelServiceRequest.builder()
            .createLabelRequest(CreateLabelRequest.builder().label(label).build())
            .build();

    service.createLabel(request);

    verify(auditLogService, times(2)).createLog(any(), any(), any(), any(), any());
  }
}
