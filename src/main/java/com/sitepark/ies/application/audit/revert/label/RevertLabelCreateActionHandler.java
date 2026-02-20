package com.sitepark.ies.application.audit.revert.label;

import com.sitepark.ies.application.audit.revert.RevertEntityActionHandler;
import com.sitepark.ies.application.label.RemoveLabelsService;
import com.sitepark.ies.application.label.RemoveLabelsServiceRequest;
import com.sitepark.ies.audit.core.service.RevertRequest;
import jakarta.inject.Inject;

public class RevertLabelCreateActionHandler implements RevertEntityActionHandler {

  private final RemoveLabelsService removeLabelsService;

  @Inject
  RevertLabelCreateActionHandler(RemoveLabelsService removeLabelsService) {
    this.removeLabelsService = removeLabelsService;
  }

  @Override
  public void revert(RevertRequest request) {
    this.removeLabelsService.removeLabels(
        RemoveLabelsServiceRequest.builder()
            .identifiers(configure -> configure.add(request.target().id()))
            .auditParentId(request.parentId())
            .build());
  }
}
