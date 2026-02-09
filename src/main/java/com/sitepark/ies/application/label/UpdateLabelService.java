package com.sitepark.ies.application.label;

import com.sitepark.ies.label.core.domain.value.AuditLogAction;
import com.sitepark.ies.label.core.domain.value.AuditLogEntityType;
import com.sitepark.ies.label.core.usecase.LabelUpdateResult.Updated;
import com.sitepark.ies.label.core.usecase.ReassignScopesToLabelsResult;
import com.sitepark.ies.label.core.usecase.UpdateLabelRequest;
import com.sitepark.ies.label.core.usecase.UpdateLabelResult;
import com.sitepark.ies.label.core.usecase.UpdateLabelUseCase;
import com.sitepark.ies.sharedkernel.audit.AuditLogService;
import com.sitepark.ies.sharedkernel.audit.CreateAuditLogEntryFailedException;
import com.sitepark.ies.sharedkernel.audit.CreateAuditLogRequest;
import jakarta.inject.Inject;
import java.io.IOException;
import java.time.Instant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
  private final AuditLogService auditLogService;

  @Inject
  UpdateLabelService(UpdateLabelUseCase updateLabelUseCase, AuditLogService auditLogService) {
    this.updateLabelUseCase = updateLabelUseCase;
    this.auditLogService = auditLogService;
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
   * @return true if label was updated, false otherwise
   * @throws com.sitepark.ies.sharedkernel.security.AccessDeniedException if label update is not
   *     allowed
   * @throws com.sitepark.ies.label.core.domain.exception.LabelNotFoundException if label does not
   *     exist
   * @throws com.sitepark.ies.sharedkernel.anchor.AnchorAlreadyExistsException if anchor already
   *     exists for a different label
   */
  public boolean updateLabel(@NotNull UpdateLabelRequest request) {

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Updating label with ID '{}'", request.label().id());
    }

    UpdateLabelResult result = this.updateLabelUseCase.updateLabel(request);

    this.createAuditLogForLabelUpdate(request, result);
    this.createAuditLogsForScopeReassignment(request, result);

    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Successfully processed label update for '{}'", result.labelId());
    }

    return result.hasAnyChanges();
  }

  private void createAuditLogForLabelUpdate(UpdateLabelRequest request, UpdateLabelResult result) {

    Updated updated = result.getLabelUpdate();
    if (updated == null) {
      return;
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Label '{}' was updated, creating audit log entry", result.labelId());
    }

    CreateAuditLogRequest auditRequest =
        new CreateAuditLogRequest(
            AuditLogEntityType.LABEL.name(),
            result.labelId(),
            updated.labelName(),
            AuditLogAction.UPDATE.name(),
            updated.revertPatch().toJson(),
            updated.patch().toJson(),
            result.timestamp(),
            request.auditParentId());

    this.auditLogService.createAuditLog(auditRequest);
  }

  private void createAuditLogsForScopeReassignment(
      UpdateLabelRequest request, UpdateLabelResult result) {

    ReassignScopesToLabelsResult.Reassigned reassigned = result.getScopeReassignment();
    if (reassigned == null) {
      return;
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Creating audit logs for scope assignments for label '{}'", result.labelId());
    }
    this.createScopeReassignmentAuditLogs(
        result.labelId(), request.label().name(), reassigned, request.auditParentId());
  }

  private void createScopeReassignmentAuditLogs(
      String labelId,
      String labelName,
      ReassignScopesToLabelsResult.Reassigned reassigned,
      @Nullable String parentId) {

    var assignments = reassigned.assignments().scopes(labelId);
    var unassignment = reassigned.unassignments().scopes(labelId);
    var timestamp = reassigned.timestamp();

    String auditParentId =
        (assignments.size() + unassignment.size()) > 1
            ? this.createBatchReassignmentLog(timestamp, parentId)
            : parentId;

    if (!assignments.isEmpty()) {
      CreateAuditLogRequest creatAssignmentAuditLogRequest =
          this.buildCreateAuditLogRequest(
              AuditLogAction.ASSIGN_SCOPES_TO_LABEL,
              labelId,
              labelName,
              assignments,
              timestamp,
              auditParentId);
      this.auditLogService.createAuditLog(creatAssignmentAuditLogRequest);
    }

    if (!unassignment.isEmpty()) {
      CreateAuditLogRequest createUnassignmentAuditLogRequest =
          this.buildCreateAuditLogRequest(
              AuditLogAction.UNASSIGN_SCOPES_FROM_LABEL,
              labelId,
              labelName,
              unassignment,
              timestamp,
              auditParentId);
      this.auditLogService.createAuditLog(createUnassignmentAuditLogRequest);
    }
  }

  private String createBatchReassignmentLog(Instant timestamp, @Nullable String parentId) {
    return this.auditLogService.createAuditLog(
        new CreateAuditLogRequest(
            AuditLogEntityType.LABEL.name(),
            null,
            null,
            AuditLogAction.BATCH_REASSIGN_SCOPES_TO_LABEL.name(),
            null,
            null,
            timestamp,
            parentId));
  }

  private CreateAuditLogRequest buildCreateAuditLogRequest(
      AuditLogAction action,
      String labelId,
      String labelName,
      java.util.List<String> scopes,
      Instant timestamp,
      @Nullable String parentId) {

    String scopesJsonArray;
    try {
      scopesJsonArray = this.auditLogService.serialize(scopes);
    } catch (IOException e) {
      throw new CreateAuditLogEntryFailedException(
          AuditLogEntityType.LABEL.name(), labelId, labelName, e);
    }

    return new CreateAuditLogRequest(
        AuditLogEntityType.LABEL.name(),
        labelId,
        labelName,
        action.name(),
        scopesJsonArray,
        scopesJsonArray,
        timestamp,
        parentId);
  }
}
