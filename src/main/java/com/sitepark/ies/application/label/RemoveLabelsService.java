package com.sitepark.ies.application.label;

import com.sitepark.ies.label.core.domain.value.AuditLogAction;
import com.sitepark.ies.label.core.domain.value.AuditLogEntityType;
import com.sitepark.ies.label.core.usecase.RemoveLabelRequest;
import com.sitepark.ies.label.core.usecase.RemoveLabelResult;
import com.sitepark.ies.label.core.usecase.RemoveLabelUseCase;
import com.sitepark.ies.sharedkernel.audit.AuditLogService;
import com.sitepark.ies.sharedkernel.audit.CreateAuditLogEntryFailedException;
import com.sitepark.ies.sharedkernel.audit.CreateAuditLogRequest;
import com.sitepark.ies.sharedkernel.base.Identifier;
import jakarta.inject.Inject;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Application Service that orchestrates label removal operations with cross-cutting concerns.
 *
 * <p>This service coordinates label removal and associated cross-cutting concerns like audit
 * logging, allowing controllers to perform complete removal operations without managing these
 * concerns themselves.
 *
 * <p><b>Responsibilities:</b>
 *
 * <ul>
 *   <li>Remove label via label-core
 *   <li>Create audit log entries for removals (only for successfully removed labels)
 * </ul>
 */
public final class RemoveLabelsService {

  private static final Logger LOGGER = LogManager.getLogger();

  private final RemoveLabelUseCase removeLabelUseCase;
  private final AuditLogService auditLogService;
  private final Clock clock;

  @Inject
  RemoveLabelsService(
      RemoveLabelUseCase removeLabelUseCase, AuditLogService auditLogService, Clock clock) {
    this.removeLabelUseCase = removeLabelUseCase;
    this.auditLogService = auditLogService;
    this.clock = clock;
  }

  /**
   * Removes one or more labels and creates audit log entries.
   *
   * <p>This method orchestrates:
   *
   * <ol>
   *   <li>Label removal (label-core) - for each identifier
   *   <li>Audit log creation (audit-core) - only for successfully removed labels
   *   <li>Batch parent log creation - if multiple labels are being removed
   * </ol>
   *
   * <p>Built-in labels (e.g., Administrator with ID "1") are skipped and logged as warnings.
   *
   * @param request the removal request containing identifiers and optional audit parent ID
   * @throws com.sitepark.ies.sharedkernel.security.AccessDeniedException if label removal is not
   *     allowed
   * @throws com.sitepark.ies.label.core.domain.exception.LabelNotFoundException if a label does not
   *     exist
   * @throws com.sitepark.ies.sharedkernel.anchor.AnchorNotFoundException if an anchor does not
   *     exist
   */
  public int removeLabels(@NotNull RemoveLabelsRequest request) {

    if (request.isEmpty()) {
      return 0;
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Removing {} label(s)", request.identifiers().size());
    }

    // Create batch parent entry if multiple labels
    Instant now = Instant.now(this.clock);
    String parentId =
        request.identifiers().size() > 1
            ? this.createBatchRemoveLog(now, request.auditParentId())
            : request.auditParentId();

    int removedCount = 0;
    int skippedCount = 0;

    // Loop through identifiers and call use case for each
    for (Identifier identifier : request.identifiers()) {
      RemoveLabelResult result =
          this.removeLabelUseCase.removeLabel(
              RemoveLabelRequest.builder().identifier(identifier).build());

      // Handle result
      switch (result) {
        case RemoveLabelResult.Removed removed -> {
          this.createRemoveAuditLog(removed, parentId);
          removedCount++;
        }
        case RemoveLabelResult.Skipped skipped -> {
          if (LOGGER.isWarnEnabled()) {
            LOGGER.warn("Skipped removal of label '{}': {}", skipped.labelId(), skipped.reason());
          }
          skippedCount++;
        }
      }
    }

    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Removed {} label(s), skipped {}", removedCount, skippedCount);
    }

    return removedCount;
  }

  private String createBatchRemoveLog(Instant timestamp, String auditParentId) {
    return this.auditLogService.createAuditLog(
        new CreateAuditLogRequest(
            AuditLogEntityType.LABEL.name(),
            null,
            null,
            com.sitepark.ies.userrepository.core.domain.value.AuditLogAction.BATCH_REMOVE.name(),
            null,
            null,
            timestamp,
            auditParentId));
  }

  private void createRemoveAuditLog(RemoveLabelResult.Removed removed, @Nullable String parentId) {
    try {
      String backwardData = this.auditLogService.serialize(removed.snapshot());

      CreateAuditLogRequest auditRequest =
          new CreateAuditLogRequest(
              AuditLogEntityType.LABEL.name(),
              removed.labelId(),
              removed.displayName(),
              AuditLogAction.REMOVE.name(),
              backwardData,
              null,
              removed.timestamp(),
              parentId);

      this.auditLogService.createAuditLog(auditRequest);

    } catch (IOException e) {
      throw new CreateAuditLogEntryFailedException(
          AuditLogEntityType.LABEL.name(), removed.labelId(), removed.displayName(), e);
    }
  }
}
