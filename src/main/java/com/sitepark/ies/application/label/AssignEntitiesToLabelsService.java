package com.sitepark.ies.application.label;

import com.sitepark.ies.application.MultiEntityAuthorizationService;
import com.sitepark.ies.label.core.domain.entity.Label;
import com.sitepark.ies.label.core.domain.value.AuditLogAction;
import com.sitepark.ies.label.core.domain.value.AuditLogEntityType;
import com.sitepark.ies.label.core.port.LabelRepository;
import com.sitepark.ies.label.core.usecase.AssignEntitiesToLabelsResult;
import com.sitepark.ies.label.core.usecase.AssignEntitiesToLabelsUseCase;
import com.sitepark.ies.sharedkernel.audit.AuditLogService;
import com.sitepark.ies.sharedkernel.audit.CreateAuditLogEntryFailedException;
import com.sitepark.ies.sharedkernel.audit.CreateAuditLogRequest;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.sharedkernel.security.AccessDeniedException;
import jakarta.inject.Inject;
import java.io.IOException;
import java.time.Instant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Application Service that orchestrates entity-to-label assignment operations with cross-cutting
 * concerns.
 *
 * <p>This service coordinates entity assignment to labels and associated cross-cutting concerns
 * like audit logging, allowing controllers to perform complete assignment operations without
 * managing these concerns themselves.
 *
 * <p><b>Responsibilities:</b>
 *
 * <ul>
 *   <li>Assign entities to labels via label-core
 *   <li>Create audit log entries for assignments
 *   <li>Handle batch operations with parent audit logs
 * </ul>
 */
public final class AssignEntitiesToLabelsService {

  private final AssignEntitiesToLabelsUseCase assignEntitiesToLabelsUseCase;
  private final MultiEntityAuthorizationService authorizationService;
  private final LabelRepository labelRepository;
  private final AuditLogService auditLogService;

  @Inject
  AssignEntitiesToLabelsService(
      AssignEntitiesToLabelsUseCase assignEntitiesToLabelsUseCase,
      MultiEntityAuthorizationService entityAccessControlService,
      LabelRepository labelRepository,
      AuditLogService auditLogService) {
    this.assignEntitiesToLabelsUseCase = assignEntitiesToLabelsUseCase;
    this.authorizationService = entityAccessControlService;
    this.labelRepository = labelRepository;
    this.auditLogService = auditLogService;
  }

  /**
   * Assigns entities to one or more labels and creates audit log entries.
   *
   * <p>This method orchestrates:
   *
   * <ol>
   *   <li>Entity assignment (label-core)
   *   <li>Audit log creation (audit-core) - only for effective assignments
   *   <li>Batch parent log creation - if multiple labels receive assignments
   * </ol>
   *
   * <p>If no effective assignments are made (all entities already assigned), no audit logs are
   * created.
   *
   * @param request the assignment request containing label identifiers, entity references, and
   *     optional audit parent ID
   * @throws com.sitepark.ies.sharedkernel.security.AccessDeniedException if entity assignment is
   *     not allowed
   * @throws com.sitepark.ies.label.core.domain.exception.LabelNotFoundException if a label does not
   *     exist
   */
  public int assignEntitiesToLabels(@NotNull AssignEntitiesToLabelsRequest request) {

    this.checkAuthorization(request);

    AssignEntitiesToLabelsResult result =
        this.assignEntitiesToLabelsUseCase.assignEntitiesToLabels(request.toUseCaseRequest());

    if (result instanceof AssignEntitiesToLabelsResult.Assigned assigned) {
      this.createAuditLogs(request, assigned);
    }

    return result.assignments().entityRefs().size();
  }

  private void checkAuthorization(AssignEntitiesToLabelsRequest request) {
    for (EntityRef entityRef : request.entityRefs()) {
      if (!this.authorizationService.isWritable(entityRef)) {
        throw new AccessDeniedException(
            "Entity " + entityRef.type() + " with id " + entityRef.id() + " not writable");
      }
    }
  }

  private void createAuditLogs(
      AssignEntitiesToLabelsRequest request, AssignEntitiesToLabelsResult.Assigned assigned) {

    var assignments = assigned.assignments();
    var timestamp = assigned.timestamp();

    String parentId =
        assignments.size() > 1
            ? this.createBatchAssignmentLog(timestamp, request.auditParentId())
            : null;

    assignments
        .labelIds()
        .forEach(
            labelId -> {
              CreateAuditLogRequest createAuditLogRequest =
                  this.buildCreateAuditLogRequest(
                      labelId, assignments.entityRefs(labelId), timestamp, parentId);
              this.auditLogService.createAuditLog(createAuditLogRequest);
            });
  }

  private String createBatchAssignmentLog(Instant timestamp, @Nullable String parentId) {
    return this.auditLogService.createAuditLog(
        new CreateAuditLogRequest(
            AuditLogEntityType.LABEL.name(),
            null,
            null,
            AuditLogAction.BATCH_ASSIGN_ENTITIES_TO_LABEL.name(),
            null,
            null,
            timestamp,
            parentId));
  }

  private CreateAuditLogRequest buildCreateAuditLogRequest(
      String labelId,
      java.util.List<EntityRef> entityRefs,
      Instant timestamp,
      @Nullable String parentId) {

    String labelDisplayName = this.labelRepository.get(labelId).map(Label::name).orElse(null);

    String entitiesJsonArray;
    try {
      entitiesJsonArray = this.auditLogService.serialize(entityRefs);
    } catch (IOException e) {
      throw new CreateAuditLogEntryFailedException(
          AuditLogEntityType.LABEL.name(), labelId, labelDisplayName, e);
    }

    return new CreateAuditLogRequest(
        AuditLogEntityType.LABEL.name(),
        labelId,
        labelDisplayName,
        AuditLogAction.ASSIGN_ENTITIES_TO_LABEL.name(),
        entitiesJsonArray,
        entitiesJsonArray,
        timestamp,
        parentId);
  }
}
