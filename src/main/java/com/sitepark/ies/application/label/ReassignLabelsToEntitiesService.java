package com.sitepark.ies.application.label;

import static java.util.stream.Stream.concat;

import com.sitepark.ies.application.MultiEntityAuthorizationService;
import com.sitepark.ies.application.MultiEntityNameResolver;
import com.sitepark.ies.label.core.domain.value.AuditLogAction;
import com.sitepark.ies.label.core.domain.value.AuditLogEntityType;
import com.sitepark.ies.label.core.usecase.ReassignLabelsToEntitiesResult;
import com.sitepark.ies.label.core.usecase.ReassignLabelsToEntitiesUseCase;
import com.sitepark.ies.sharedkernel.audit.AuditLogService;
import com.sitepark.ies.sharedkernel.audit.CreateAuditLogEntryFailedException;
import com.sitepark.ies.sharedkernel.audit.CreateAuditLogRequest;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.sharedkernel.security.AccessDeniedException;
import jakarta.inject.Inject;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Application Service that orchestrates labels-to-entities reassignment operations with
 * cross-cutting concerns.
 *
 * <p>This service coordinates labels reassignment to entites and associated cross-cutting concerns
 * like audit logging, allowing controllers to perform complete reassignment operations without
 * managing these concerns themselves.
 *
 * <p><b>Responsibilities:</b>
 *
 * <ul>
 *   <li>Reassign labels to entities via label-core
 *   <li>Create audit log entries for reassignments
 *   <li>Handle batch operations with parent audit logs
 * </ul>
 */
public final class ReassignLabelsToEntitiesService {

  private final ReassignLabelsToEntitiesUseCase reassignLabelsToEntitiesUseCase;
  private final MultiEntityAuthorizationService authorizationService;
  private final MultiEntityNameResolver multiEntityNameResolver;
  private final AuditLogService auditLogService;

  @Inject
  ReassignLabelsToEntitiesService(
      ReassignLabelsToEntitiesUseCase reassignLabelsToEntitiesUseCase,
      MultiEntityAuthorizationService authorizationService,
      MultiEntityNameResolver multiEntityNameResolver,
      AuditLogService auditLogService) {
    this.reassignLabelsToEntitiesUseCase = reassignLabelsToEntitiesUseCase;
    this.authorizationService = authorizationService;
    this.multiEntityNameResolver = multiEntityNameResolver;
    this.auditLogService = auditLogService;
  }

  /**
   * Reassigns entities from one or more labels and creates audit log entries.
   *
   * <p>This method orchestrates:
   *
   * <ol>
   *   <li>Entity reassignment (label-core)
   *   <li>Audit log creation (audit-core) - only for effective reassignments
   *   <li>Batch parent log creation - if multiple labels have reassignments
   * </ol>
   *
   * <p>If no effective reassignments are made (entities not assigned), no audit logs are created.
   *
   * @param request the reassignment request containing label identifiers, entity references, and
   *     optional audit parent ID
   * @throws AccessDeniedException if entity unassignment is not allowed
   * @throws com.sitepark.ies.label.core.domain.exception.LabelNotFoundException if a label does not
   *     exist
   */
  public int reassignEntitiesFromLabels(@NotNull ReassignLabelsToEntitiesRequest request) {

    this.checkAuthorization(request);

    ReassignLabelsToEntitiesResult result =
        this.reassignLabelsToEntitiesUseCase.reassignLabelsToEntities(request.toUseCaseRequest());

    if (result instanceof ReassignLabelsToEntitiesResult.Reassigned reassigned) {
      this.createAuditLogs(request, reassigned);
      return reassigned.assignments().countAssignments();
    } else {
      return 0;
    }
  }

  private void checkAuthorization(ReassignLabelsToEntitiesRequest request) {
    for (EntityRef entityRef : request.entityRefs()) {
      if (!this.authorizationService.isWritable(entityRef)) {
        throw new AccessDeniedException(
            "Entity " + entityRef.type() + " with id " + entityRef.id() + " not writable");
      }
    }
  }

  private void createAuditLogs(
      ReassignLabelsToEntitiesRequest request, ReassignLabelsToEntitiesResult.Reassigned result) {

    Map<EntityRef, String> entityNames = resolveEntityNames(result);

    var unassignments = result.unassignments();
    var assignments = result.assignments();
    var timestamp = result.timestamp();

    String parentId =
        (unassignments.size() + assignments.size()) > 1
            ? this.createBatchReassignmentLog(timestamp, request.auditParentId())
            : null;

    unassignments
        .entityRefs()
        .forEach(
            entityRef -> {
              CreateAuditLogRequest createAuditLogRequest =
                  this.buildCreateAuditLogRequest(
                      AuditLogAction.UNASSIGN_ENTITIES_FROM_LABEL,
                      entityRef,
                      entityNames.get(entityRef),
                      unassignments.labelIds(entityRef),
                      timestamp,
                      parentId);
              this.auditLogService.createAuditLog(createAuditLogRequest);
            });

    assignments
        .entityRefs()
        .forEach(
            entityRef -> {
              CreateAuditLogRequest createAuditLogRequest =
                  this.buildCreateAuditLogRequest(
                      AuditLogAction.ASSIGN_ENTITIES_TO_LABEL,
                      entityRef,
                      entityNames.get(entityRef),
                      unassignments.labelIds(entityRef),
                      timestamp,
                      parentId);
              this.auditLogService.createAuditLog(createAuditLogRequest);
            });
  }

  private String createBatchReassignmentLog(Instant timestamp, @Nullable String parentId) {
    return this.auditLogService.createAuditLog(
        new CreateAuditLogRequest(
            AuditLogEntityType.LABEL.name(),
            null,
            null,
            AuditLogAction.BATCH_REASSIGN_LABELS_TO_ENTITIES.name(),
            null,
            null,
            timestamp,
            parentId));
  }

  private CreateAuditLogRequest buildCreateAuditLogRequest(
      AuditLogAction action,
      EntityRef entityRef,
      String entityName,
      java.util.List<String> labels,
      Instant timestamp,
      @Nullable String parentId) {

    String labelsJsonArray;
    try {
      labelsJsonArray = this.auditLogService.serialize(labels);
    } catch (IOException e) {
      throw new CreateAuditLogEntryFailedException(entityRef.type(), entityRef.id(), entityName, e);
    }

    return new CreateAuditLogRequest(
        entityRef.type(),
        entityRef.id(),
        entityName,
        action.name(),
        labelsJsonArray,
        labelsJsonArray,
        timestamp,
        parentId);
  }

  private Map<EntityRef, String> resolveEntityNames(
      ReassignLabelsToEntitiesResult.Reassigned result) {
    Set<EntityRef> entityRefs =
        concat(
                result.assignments().entityRefs().stream(),
                result.unassignments().entityRefs().stream())
            .collect(Collectors.toSet());
    return multiEntityNameResolver.resolveNames(entityRefs);
  }
}
