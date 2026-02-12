package com.sitepark.ies.application.label;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.MultiEntityNameResolver;
import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.label.core.domain.entity.Label;
import com.sitepark.ies.label.core.usecase.LabelUpdateResult.Updated;
import com.sitepark.ies.label.core.usecase.ReassignScopesToLabelsResult;
import com.sitepark.ies.label.core.usecase.UpdateLabelResult;
import com.sitepark.ies.label.core.usecase.UpdateLabelUseCase;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
public final class UpdateLabelService {

  private static final Logger LOGGER = LogManager.getLogger();

  private final UpdateLabelUseCase updateLabelUseCase;
  private final MultiEntityNameResolver multiEntityNameResolver;
  private final ApplicationAuditLogServiceFactory auditLogServiceFactory;

  @Inject
  UpdateLabelService(
      UpdateLabelUseCase updateLabelUseCase,
      MultiEntityNameResolver multiEntityNameResolver,
      ApplicationAuditLogServiceFactory auditLogServiceFactory) {
    this.updateLabelUseCase = updateLabelUseCase;
    this.auditLogServiceFactory = auditLogServiceFactory;
    this.multiEntityNameResolver = multiEntityNameResolver;
  }

  /**
   * Updates an existing label and creates an audit log entry.
   *
   * <p>This method orchestrates:
   *
   * <ol>
   *   <li>Label update (label-core)
   *   <li>Audit log creation (audit-core) - only if changes were made
   * </ol>
   *
   * <p>If no changes are detected (label data identical to stored data), the update is skipped and
   * no audit log entry is created.
   *
   * @param request contains label data, scope IDs, and optional audit parent ID
   * @return true if the label was updated, false otherwise
   * @throws com.sitepark.ies.sharedkernel.security.AccessDeniedException if label update is not
   *     allowed
   * @throws com.sitepark.ies.label.core.domain.exception.LabelNotFoundException if the label does
   *     not exist
   * @throws com.sitepark.ies.sharedkernel.anchor.AnchorAlreadyExistsException if anchor already
   *     exists for a different label
   */
  public boolean updateLabel(@NotNull UpdateLabelServiceRequest request) {

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Updating label with ID '{}'", request.updateLabelRequest().label().id());
    }

    UpdateLabelResult result = this.updateLabelUseCase.updateLabel(request.updateLabelRequest());

    this.createAuditLogForLabelUpdate(result, request.auditParentId());
    this.createAuditLogsForScopeReassignment(result, request.auditParentId());

    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Successfully processed label update for '{}'", result.labelId());
    }

    return result.hasAnyChanges();
  }

  protected void createAuditLogs(UpdateLabelResult result, String auditParentId) {
    this.createAuditLogForLabelUpdate(result, auditParentId);
    this.createAuditLogsForScopeReassignment(result, auditParentId);
  }

  private void createAuditLogForLabelUpdate(UpdateLabelResult result, String auditParentId) {

    Updated updated = result.getLabelUpdate();
    if (updated == null) {
      return;
    }

    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(result.timestamp(), auditParentId);

    auditLogService.createLog(
        EntityRef.of(Label.class, result.labelId()),
        updated.labelName(),
        AuditLogAction.UPDATE,
        updated.revertPatch().toJson(),
        updated.patch().toJson());
  }

  private void createAuditLogsForScopeReassignment(UpdateLabelResult result, String auditParentId) {

    ReassignScopesToLabelsResult.Reassigned reassigned = result.getScopeReassignment();
    if (reassigned == null) {
      return;
    }

    String labelName =
        result.getLabelUpdate() != null
            ? result.getLabelUpdate().labelName()
            : this.multiEntityNameResolver.resolveName(EntityRef.of(Label.class, result.labelId()));

    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(result.timestamp(), auditParentId);

    var assignments = reassigned.assignments().scopes();
    if (!assignments.isEmpty()) {
      auditLogService.createLog(
          EntityRef.of(Label.class, result.labelId()),
          labelName,
          AuditLogAction.ASSIGN_SCOPES_TO_LABEL,
          assignments,
          assignments);
    }

    var unassignment = reassigned.unassignments().scopes();
    if (!unassignment.isEmpty()) {
      auditLogService.createLog(
          EntityRef.of(Label.class, result.labelId()),
          labelName,
          AuditLogAction.UNASSIGN_SCOPES_FROM_LABEL,
          unassignment,
          unassignment);
    }
  }
}
