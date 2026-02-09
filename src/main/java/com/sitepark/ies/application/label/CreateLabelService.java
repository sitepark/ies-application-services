package com.sitepark.ies.application.label;

import com.sitepark.ies.label.core.domain.entity.Label;
import com.sitepark.ies.label.core.domain.value.AuditLogAction;
import com.sitepark.ies.label.core.domain.value.AuditLogEntityType;
import com.sitepark.ies.label.core.usecase.CreateLabelRequest;
import com.sitepark.ies.label.core.usecase.CreateLabelResult;
import com.sitepark.ies.label.core.usecase.CreateLabelUseCase;
import com.sitepark.ies.sharedkernel.audit.AuditLogService;
import com.sitepark.ies.sharedkernel.audit.CreateAuditLogEntryFailedException;
import com.sitepark.ies.sharedkernel.audit.CreateAuditLogRequest;
import jakarta.inject.Inject;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

/**
 * Application Service that orchestrates label creation operations with cross-cutting concerns.
 *
 * <p>This service coordinates label creation and associated cross-cutting concerns like audit
 * logging, allowing controllers to perform complete creation operations without managing these
 * concerns themselves.
 *
 * <p><b>Responsibilities:</b>
 *
 * <ul>
 *   <li>Create label via label-core
 *   <li>Create audit log entries for label creation
 * </ul>
 */
public final class CreateLabelService {

  private final CreateLabelUseCase createLabelUseCase;
  private final AuditLogService auditLogService;

  @Inject
  CreateLabelService(CreateLabelUseCase createLabelUseCase, AuditLogService auditLogService) {
    this.createLabelUseCase = createLabelUseCase;
    this.auditLogService = auditLogService;
  }

  /**
   * Creates a new label and creates audit log entry.
   *
   * <p>This method orchestrates:
   *
   * <ol>
   *   <li>Label creation (label-core)
   *   <li>Audit log creation (audit-core) - for label creation
   * </ol>
   *
   * @param request the creation request containing label data, scope IDs, and optional audit parent
   *     ID
   * @return the created label ID
   * @throws com.sitepark.ies.sharedkernel.security.AccessDeniedException if label creation is not
   *     allowed
   * @throws com.sitepark.ies.sharedkernel.anchor.AnchorAlreadyExistsException if anchor already
   *     exists
   */
  public String createLabel(@NotNull CreateLabelRequest request) {

    // 1. Create label via label-core
    CreateLabelResult result = this.createLabelUseCase.createLabel(request);

    // 2. Create audit log for label creation
    this.createLabelCreationAuditLog(result);

    return result.labelId();
  }

  private void createLabelCreationAuditLog(CreateLabelResult result) {

    Label label = result.snapshot().label();
    String forwardData;
    try {
      forwardData = this.auditLogService.serialize(result.snapshot());
    } catch (IOException e) {
      throw new CreateAuditLogEntryFailedException(
          AuditLogEntityType.LABEL.name(), result.labelId(), label.name(), e);
    }

    CreateAuditLogRequest auditRequest =
        new CreateAuditLogRequest(
            AuditLogEntityType.LABEL.name(),
            result.labelId(),
            label.name(),
            AuditLogAction.CREATE.name(),
            null,
            forwardData,
            result.timestamp(),
            null);

    this.auditLogService.createAuditLog(auditRequest);
  }
}
