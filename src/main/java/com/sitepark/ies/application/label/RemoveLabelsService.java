package com.sitepark.ies.application.label;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.audit.AuditBatchLogAction;
import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.label.core.domain.entity.Label;
import com.sitepark.ies.label.core.usecase.RemoveLabelRequest;
import com.sitepark.ies.label.core.usecase.RemoveLabelResult;
import com.sitepark.ies.label.core.usecase.RemoveLabelUseCase;
import com.sitepark.ies.sharedkernel.base.Identifier;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import jakarta.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

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
  private final ApplicationAuditLogServiceFactory auditLogServiceFactory;
  private final Clock clock;

  @Inject
  RemoveLabelsService(
      RemoveLabelUseCase removeLabelUseCase,
      ApplicationAuditLogServiceFactory auditLogServiceFactory,
      Clock clock) {
    this.removeLabelUseCase = removeLabelUseCase;
    this.auditLogServiceFactory = auditLogServiceFactory;
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
  public int removeLabels(@NotNull RemoveLabelsServiceRequest request) {

    if (request.identifiers().isEmpty()) {
      return 0;
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Removing {} label(s)", request.identifiers().size());
    }

    Instant timestamp = Instant.now(this.clock);

    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(timestamp, request.auditParentId());
    String parentId =
        request.identifiers().size() > 1
            ? auditLogService.createBatchLog(Label.class, AuditBatchLogAction.BATCH_REMOVE)
            : request.auditParentId();
    auditLogService.updateParentId(parentId);

    int removedCount = 0;
    int skippedCount = 0;

    for (Identifier identifier : request.identifiers()) {
      RemoveLabelResult result =
          this.removeLabelUseCase.removeLabel(
              RemoveLabelRequest.builder().identifier(identifier).build());

      switch (result) {
        case RemoveLabelResult.Removed removed -> {
          auditLogService.createLog(
              EntityRef.of(Label.class, removed.labelId()),
              removed.labelName(),
              AuditLogAction.REMOVE,
              removed.snapshot(),
              null);
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
}
