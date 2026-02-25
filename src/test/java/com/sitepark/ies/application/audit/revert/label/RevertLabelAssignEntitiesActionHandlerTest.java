package com.sitepark.ies.application.audit.revert.label;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.application.label.UnassignLabelsFromEntitiesService;
import com.sitepark.ies.audit.core.domain.value.AuditLogTarget;
import com.sitepark.ies.audit.core.service.AuditLogService;
import com.sitepark.ies.audit.core.service.RevertRequest;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RevertLabelAssignEntitiesActionHandlerTest {

  private AuditLogService auditLogService;
  private UnassignLabelsFromEntitiesService unassignLabelsFromEntitiesService;
  private RevertRequest request;
  private RevertLabelAssignEntitiesActionHandler handler;

  @BeforeEach
  void setUp() {
    this.auditLogService = mock();
    this.unassignLabelsFromEntitiesService = mock();
    this.request = mock();

    AuditLogTarget target = mock();
    when(this.request.target()).thenReturn(target);
    when(target.id()).thenReturn("1");
    when(this.request.parentId()).thenReturn("parent-id");
    when(this.request.id()).thenReturn("log-id");

    when(target.toEntityRef()).thenReturn(mock());

    this.handler =
        new RevertLabelAssignEntitiesActionHandler(
            this.auditLogService, this.unassignLabelsFromEntitiesService);
  }

  @Test
  void testRevertCallsUnassignLabelsFromEntitiesService() throws IOException {
    when(this.auditLogService.getBackwardDataList(eq("log-id"), eq(String.class)))
        .thenReturn(List.of("1"));

    this.handler.revert(this.request);

    verify(this.unassignLabelsFromEntitiesService).unassignLabelsFromEntities(any());
  }

  @Test
  void testRevertWithDeserializationFailureThrowsRevertFailedException() throws IOException {
    when(this.auditLogService.getBackwardDataList(eq("log-id"), eq(String.class)))
        .thenThrow(new IOException("deserialization error"));

    assertThrows(
        RevertFailedException.class,
        () -> this.handler.revert(this.request),
        "revert() should throw RevertFailedException when deserialization of LabelEntityAssignment"
            + " fails");
  }
}
