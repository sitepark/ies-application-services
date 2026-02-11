package com.sitepark.ies.application.audit.revert.label;

import com.sitepark.ies.application.audit.revert.RevertEntityActionHandler;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.label.core.usecase.RemoveLabelRequest;
import com.sitepark.ies.label.core.usecase.RemoveLabelUseCase;
import jakarta.inject.Inject;

public class RevertLabelCreateActionHandler implements RevertEntityActionHandler {

  private final RemoveLabelUseCase removeLabelUseCase;

  @Inject
  RevertLabelCreateActionHandler(RemoveLabelUseCase removeLabelUseCase) {
    this.removeLabelUseCase = removeLabelUseCase;
  }

  @Override
  public void revert(RevertRequest request) {
    this.removeLabelUseCase.removeLabel(
        RemoveLabelRequest.builder().id(request.target().id()).build());
  }
}
