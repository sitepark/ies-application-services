package com.sitepark.ies.application.audit.revert.label;

import com.sitepark.ies.application.audit.revert.RevertEntityActionHandler;
import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.application.label.AssignLabelsToEntitiesService;
import com.sitepark.ies.application.label.AssignLabelsToEntitiesServiceRequest;
import com.sitepark.ies.audit.core.service.AuditLogService;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.label.core.usecase.AssignLabelsToEntitiesRequest;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.List;

public class RevertLabelUnassignEntitiesActionHandler implements RevertEntityActionHandler {

  private final AuditLogService auditLogService;
  private final AssignLabelsToEntitiesService assignLabelsToEntitiesService;

  @Inject
  RevertLabelUnassignEntitiesActionHandler(
      AuditLogService auditLogService,
      AssignLabelsToEntitiesService assignLabelsToEntitiesService) {
    this.auditLogService = auditLogService;
    this.assignLabelsToEntitiesService = assignLabelsToEntitiesService;
  }

  @Override
  public void revert(RevertRequest request) {
    try {
      List<String> labelIds = this.auditLogService.getBackwardDataList(request.id(), String.class);
      this.assignLabelsToEntitiesService.assignLabelsToEntities(
          AssignLabelsToEntitiesServiceRequest.builder()
              .assignEntitiesToLabelsRequest(
                  AssignLabelsToEntitiesRequest.builder()
                      .entityRefs(b -> b.set(request.target().toEntityRef()))
                      .labelIdentifiers(b -> b.ids(labelIds))
                      .build())
              .auditParentId(request.parentId())
              .build());
    } catch (IOException e) {
      throw new RevertFailedException(request, "Failed to deserialize LabelEntityAssignment", e);
    }
  }
}
