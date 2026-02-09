package com.sitepark.ies.application.label;

import com.sitepark.ies.application.MultiEntityAuthorizationService;
import com.sitepark.ies.label.core.domain.entity.Label;
import com.sitepark.ies.label.core.domain.value.AuditLogAction;
import com.sitepark.ies.label.core.domain.value.AuditLogEntityType;
import com.sitepark.ies.label.core.port.LabelRepository;
import com.sitepark.ies.label.core.usecase.UnassignEntitiesFromLabelsRequest;
import com.sitepark.ies.label.core.usecase.UnassignEntitiesFromLabelsUseCase;
import com.sitepark.ies.label.core.usecase.UnassignEntitiesToLabelsResult;
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
 * Application Service that orchestrates entity-from-label unassignment operations with
 * cross-cutting concerns.
 *
 * <p>This service coordinates entity unassignment from labels and associated cross-cutting concerns
 * like audit logging, allowing controllers to perform complete unassignment operations without
 * managing these concerns themselves.
 *
 * <p><b>Responsibilities:</b>
 *
 * <ul>
 *   <li>Unassign entities from labels via label-core
 *   <li>Create audit log entries for unassignments
 *   <li>Handle batch operations with parent audit logs
 * </ul>
 */
public final class UnassignEntitiesFromLabelsService {

  private final UnassignEntitiesFromLabelsUseCase unassignEntitiesFromLabelsUseCase;
  private final MultiEntityAuthorizationService authorizationService;
  private final LabelRepository labelRepository;
  private final AuditLogService auditLogService;

  @Inject
  UnassignEntitiesFromLabelsService(
      UnassignEntitiesFromLabelsUseCase unassignEntitiesFromLabelsUseCase,
      MultiEntityAuthorizationService authorizationService,
      LabelRepository labelRepository,
      AuditLogService auditLogService) {
    this.unassignEntitiesFromLabelsUseCase = unassignEntitiesFromLabelsUseCase;
    this.authorizationService = authorizationService;
    this.labelRepository = labelRepository;
    this.auditLogService = auditLogService;
  }

  /**
   * Unassigns entities from one or more labels and creates audit log entries.
   *
   * <p>This method orchestrates:
   *
   * <ol>
   *   <li>Entity unassignment (label-core)
   *   <li>Audit log creation (audit-core) - only for effective unassignments
   *   <li>Batch parent log creation - if multiple labels have unassignments
   * </ol>
   *
   * <p>If no effective unassignments are made (entities not assigned), no audit logs are created.
   *
   * @param request the unassignment request containing label identifiers, entity references, and
   *     optional audit parent ID
   * @throws com.sitepark.ies.sharedkernel.security.AccessDeniedException if entity unassignment is
   *     not allowed
   * @throws com.sitepark.ies.label.core.domain.exception.LabelNotFoundException if a label does not
   *     exist
   */
  public int unassignEntitiesFromLabels(@NotNull UnassignEntitiesFromLabelsRequest request) {

    this.checkAuthorization(request);

    UnassignEntitiesToLabelsResult result =
        this.unassignEntitiesFromLabelsUseCase.unassignEntitiesFromLabels(request);

    if (result instanceof UnassignEntitiesToLabelsResult.Unassigned unassigned) {
      this.createAuditLogs(unassigned);
    }

    return result.unassignments().size();
  }

  private void checkAuthorization(UnassignEntitiesFromLabelsRequest request) {
    for (EntityRef entityRef : request.entityRefs()) {
      if (!this.authorizationService.isWritable(entityRef)) {
        throw new AccessDeniedException(
            "Entity " + entityRef.type() + " with id " + entityRef.id() + " not writable");
      }
    }
  }

  private void createAuditLogs(UnassignEntitiesToLabelsResult.Unassigned unassigned) {

    var unassignments = unassigned.unassignments();
    var timestamp = unassigned.timestamp();

    String parentId =
        unassignments.size() > 1 ? this.createBatchUnassignmentLog(timestamp, null) : null;

    unassignments
        .labelIds()
        .forEach(
            labelId -> {
              CreateAuditLogRequest createAuditLogRequest =
                  this.buildCreateAuditLogRequest(
                      labelId, unassignments.entityRefs(labelId), timestamp, parentId);
              this.auditLogService.createAuditLog(createAuditLogRequest);
            });
  }

  private String createBatchUnassignmentLog(Instant timestamp, @Nullable String parentId) {
    return this.auditLogService.createAuditLog(
        new CreateAuditLogRequest(
            AuditLogEntityType.LABEL.name(),
            null,
            null,
            AuditLogAction.BATCH_UNASSIGN_ENTITIES_FROM_LABEL.name(),
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
        AuditLogAction.UNASSIGN_ENTITIES_FROM_LABEL.name(),
        entitiesJsonArray,
        entitiesJsonArray,
        timestamp,
        parentId);
  }
}
