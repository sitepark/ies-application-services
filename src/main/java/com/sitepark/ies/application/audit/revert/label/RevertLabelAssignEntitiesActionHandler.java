package com.sitepark.ies.application.audit.revert.label;

import com.sitepark.ies.application.audit.revert.RevertEntityActionHandler;
import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.audit.core.service.AuditLogService;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.label.core.domain.value.LabelEntityAssignment;
import com.sitepark.ies.label.core.usecase.UnassignEntitiesFromLabelsRequest;
import com.sitepark.ies.label.core.usecase.UnassignEntitiesFromLabelsUseCase;
import jakarta.inject.Inject;
import java.io.IOException;

public class RevertLabelAssignEntitiesActionHandler implements RevertEntityActionHandler {

  private final AuditLogService auditLogService;

  private final UnassignEntitiesFromLabelsUseCase unassignEntitiesFromLabelsUseCase;

  @Inject
  RevertLabelAssignEntitiesActionHandler(
      AuditLogService auditLogService,
      UnassignEntitiesFromLabelsUseCase unassignEntitiesFromLabelsUseCase) {
    this.auditLogService = auditLogService;
    this.unassignEntitiesFromLabelsUseCase = unassignEntitiesFromLabelsUseCase;
  }

  @Override
  public void revert(RevertRequest request) {
    try {
      LabelEntityAssignment assignments =
          this.auditLogService.deserialize(request.backwardData(), LabelEntityAssignment.class);

      for (String labelId : assignments.labelIds()) {
        this.unassignEntitiesFromLabelsUseCase.unassignEntitiesFromLabels(
            UnassignEntitiesFromLabelsRequest.builder()
                .entityRefs(b -> b.set(assignments.entityRefs(labelId)))
                .labelIdentifiers(b -> b.id(labelId))
                .build());
      }
    } catch (IOException e) {
      throw new RevertFailedException(request, "Failed to deserialize LabelEntityAssignment", e);
    }
  }
}
