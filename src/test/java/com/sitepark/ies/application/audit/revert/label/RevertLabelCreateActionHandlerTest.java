package com.sitepark.ies.application.audit.revert.label;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.label.RemoveLabelsService;
import com.sitepark.ies.audit.core.domain.value.AuditLogTarget;
import com.sitepark.ies.audit.core.service.RevertRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RevertLabelCreateActionHandlerTest {

  private RemoveLabelsService removeLabelsService;
  private RevertRequest request;
  private RevertLabelCreateActionHandler handler;

  @BeforeEach
  void setUp() {
    this.removeLabelsService = mock();
    this.request = mock();

    AuditLogTarget target = mock();
    when(this.request.target()).thenReturn(target);
    when(target.id()).thenReturn("1");
    when(this.request.parentId()).thenReturn("parent-id");

    this.handler = new RevertLabelCreateActionHandler(this.removeLabelsService);
  }

  @Test
  void testRevertCallsRemoveLabelsService() {
    this.handler.revert(this.request);

    verify(this.removeLabelsService).removeLabels(any());
  }
}
