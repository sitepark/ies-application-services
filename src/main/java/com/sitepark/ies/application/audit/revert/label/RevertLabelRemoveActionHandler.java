package com.sitepark.ies.application.audit.revert.label;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.application.audit.revert.RevertEntityActionHandler;
import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.audit.core.domain.value.AuditLogTarget;
import com.sitepark.ies.audit.core.service.AuditLogService;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.label.core.domain.entity.Label;
import com.sitepark.ies.label.core.domain.value.LabelSnapshot;
import com.sitepark.ies.label.core.usecase.RestoreLabelRequest;
import com.sitepark.ies.label.core.usecase.RestoreLabelResult;
import com.sitepark.ies.label.core.usecase.RestoreLabelUseCase;
import jakarta.inject.Inject;
import java.io.IOException;

public class RevertLabelRemoveActionHandler implements RevertEntityActionHandler {

  private final ApplicationAuditLogServiceFactory auditLogServiceFactory;
  private final AuditLogService auditLogService;
  private final RestoreLabelUseCase restoreLabelUseCase;

  @Inject
  RevertLabelRemoveActionHandler(
      ApplicationAuditLogServiceFactory auditLogServiceFactory,
      AuditLogService auditLogService,
      RestoreLabelUseCase restoreLabelUseCase) {
    this.auditLogServiceFactory = auditLogServiceFactory;
    this.auditLogService = auditLogService;
    this.restoreLabelUseCase = restoreLabelUseCase;
  }

  @Override
  public void revert(RevertRequest request) {
    try {
      LabelSnapshot restoreData =
          this.auditLogService.deserialize(request.backwardData(), LabelSnapshot.class);
      RestoreLabelResult result =
          this.restoreLabelUseCase.restoreLabel(new RestoreLabelRequest(restoreData, null));
      if (result instanceof RestoreLabelResult.Restored restored) {
        this.createRestoreAuditLog(restored, request.parentId());
      }
    } catch (IOException e) {
      throw new RevertFailedException(request, "Failed to deserialize label-snapshot", e);
    }
  }

  private void createRestoreAuditLog(RestoreLabelResult.Restored restored, String auditParentId) {

    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(restored.timestamp(), auditParentId);

    auditLogService.createLog(
        AuditLogTarget.of(Label.class, restored.labelId(), restored.snapshot().label().name()),
        AuditLogAction.RESTORE,
        null,
        restored.snapshot());
  }
}
