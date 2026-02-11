package com.sitepark.ies.application.label;

import com.sitepark.ies.label.core.usecase.UpsertLabelResult;
import com.sitepark.ies.label.core.usecase.UpsertLabelUseCase;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;

/**
 * Application Service that orchestrates label update operations with cross-cutting concerns.
 *
 * <p>This service coordinates label updates and associated cross-cutting concerns like audit
 * logging, allowing controllers to perform complete update operations without managing these
 * concerns themselves.
 *
 * <p><b>Responsibilities:</b>
 *
 * <ul>
 *   <li>Update label via label-core
 *   <li>Create audit log entries for changes (only if changes were made)
 * </ul>
 */
public final class UpsertLabelService {

  private final UpsertLabelUseCase upsertLabelUseCase;
  private final CreateLabelService createLabelService;
  private final UpdateLabelService updateLabelService;

  @Inject
  UpsertLabelService(
      UpsertLabelUseCase upsertLabelUseCase,
      CreateLabelService createLabelService,
      UpdateLabelService updateLabelService) {
    this.upsertLabelUseCase = upsertLabelUseCase;
    this.createLabelService = createLabelService;
    this.updateLabelService = updateLabelService;
  }

  public boolean upsertLabel(@NotNull UpsertLabelServiceRequest request) {

    UpsertLabelResult result = this.upsertLabelUseCase.upsertLabel(request.upsertLabelRequest());

    if (result instanceof UpsertLabelResult.Updated updated) {
      if (!updated.updateLabelResult().hasAnyChanges()) {
        return false;
      }
      this.updateLabelService.createAuditLogForLabelUpdate(
          updated.updateLabelResult(), request.auditParentId());
      return true;
    } else if (result instanceof UpsertLabelResult.Created created) {
      this.createLabelService.createLabelCreationAuditLog(
          created.updateLabelResult(), request.auditParentId());
      return true;
    }

    return false;
  }
}
