package com.sitepark.ies.application.label;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.label.core.domain.entity.Label;
import com.sitepark.ies.label.core.usecase.AssignScopesToLabelsResult;
import com.sitepark.ies.label.core.usecase.CreateLabelResult;
import com.sitepark.ies.label.core.usecase.CreateLabelUseCase;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import jakarta.inject.Inject;
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
  private final ApplicationAuditLogServiceFactory auditLogServiceFactory;

  @Inject
  CreateLabelService(
      CreateLabelUseCase createLabelUseCase,
      ApplicationAuditLogServiceFactory auditLogServiceFactory) {
    this.createLabelUseCase = createLabelUseCase;
    this.auditLogServiceFactory = auditLogServiceFactory;
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
  public String createLabel(@NotNull CreateLabelServiceRequest request) {

    CreateLabelResult result = this.createLabelUseCase.createLabel(request.createLabelRequest());
    this.createAuditLogs(result, request.auditParentId());

    return result.labelId();
  }

  protected void createAuditLogs(CreateLabelResult result, String auditLogParentId) {
    createAuditLogForLabelUpdate(result, auditLogParentId);
    createAuditLogsForScopeReassignment(result, auditLogParentId);
  }

  private void createAuditLogForLabelUpdate(CreateLabelResult result, String auditLogParentId) {

    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(result.timestamp(), auditLogParentId);

    Label label = result.snapshot().label();
    auditLogService.createLog(
        EntityRef.of(Label.class, result.labelId()),
        label.name(),
        AuditLogAction.CREATE,
        null,
        result.snapshot());
  }

  private void createAuditLogsForScopeReassignment(CreateLabelResult result, String auditParentId) {

    if (!(result.scopeAssignmentResult() instanceof AssignScopesToLabelsResult.Assigned assign)) {
      return;
    }

    String labelName = result.snapshot().label().name();

    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(result.timestamp(), auditParentId);

    var assignments = assign.assignments().scopes();
    auditLogService.createLog(
        EntityRef.of(Label.class, result.labelId()),
        labelName,
        AuditLogAction.ASSIGN_SCOPES_TO_LABEL,
        assignments,
        assignments);
  }
}
