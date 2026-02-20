package com.sitepark.ies.application.audit.revert.label;

import com.sitepark.ies.application.audit.revert.RevertEntityActionHandler;
import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.application.label.UnassignLabelsFromEntitiesService;
import com.sitepark.ies.application.label.UnassignLabelsFromEntitiesServiceRequest;
import com.sitepark.ies.audit.core.service.AuditLogService;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.label.core.usecase.UnassignLabelsFromEntitiesRequest;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.List;

public class RevertLabelAssignEntitiesActionHandler implements RevertEntityActionHandler {

  private final AuditLogService auditLogService;
  private final UnassignLabelsFromEntitiesService unassignLabelsFromEntitiesService;

  @Inject
  RevertLabelAssignEntitiesActionHandler(
      AuditLogService auditLogService,
      UnassignLabelsFromEntitiesService unassignLabelsFromEntitiesService) {
    this.auditLogService = auditLogService;
    this.unassignLabelsFromEntitiesService = unassignLabelsFromEntitiesService;
  }

  @Override
  public void revert(RevertRequest request) {
    try {
      List<String> labelIds = this.auditLogService.getBackwardDataList(request.id(), String.class);

      this.unassignLabelsFromEntitiesService.unassignEntitiesFromLabels(
          UnassignLabelsFromEntitiesServiceRequest.builder()
              .unassignEntitiesFromLabelsRequest(
                  UnassignLabelsFromEntitiesRequest.builder()
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
