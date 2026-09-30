package com.sitepark.ies.application.audit.revert.label;

import com.sitepark.ies.application.audit.revert.RevertEntityActionHandler;
import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.application.label.UpdateLabelService;
import com.sitepark.ies.application.label.UpdateLabelServiceRequest;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.label.core.domain.entity.Label;
import com.sitepark.ies.label.core.port.LabelRepository;
import com.sitepark.ies.label.core.usecase.UpdateLabelRequest;
import com.sitepark.ies.sharedkernel.patch.PatchDocument;
import com.sitepark.ies.sharedkernel.patch.PatchService;
import com.sitepark.ies.sharedkernel.patch.PatchServiceFactory;
import jakarta.inject.Inject;
import java.util.Objects;

public class RevertLabelUpdateActionHandler implements RevertEntityActionHandler {

  private final UpdateLabelService updateLabelService;
  private final PatchService<Label> patchService;
  private final LabelRepository repository;

  @Inject
  RevertLabelUpdateActionHandler(
      UpdateLabelService updateLabelService,
      PatchServiceFactory patchServiceFactory,
      LabelRepository repository) {
    this.updateLabelService = updateLabelService;
    this.patchService = patchServiceFactory.createPatchService(Label.class);
    this.repository = repository;
  }

  @Override
  public void revert(RevertRequest request) {
    PatchDocument patch =
        this.patchService.parsePatch(
            Objects.requireNonNull(request.backwardData(), "revert needs the backward data"));
    Label label =
        this.repository
            .get(Objects.requireNonNull(request.target().id(), "revert target has an id"))
            .orElseThrow(
                () ->
                    new RevertFailedException(
                        request, "Label not found: " + request.target().id()));
    Label patchedLabel = this.patchService.applyPatch(label, patch);
    this.updateLabelService.updateLabel(
        UpdateLabelServiceRequest.builder()
            .updateLabelRequest(UpdateLabelRequest.builder().label(patchedLabel).build())
            .auditParentId(request.parentId())
            .build());
  }
}
