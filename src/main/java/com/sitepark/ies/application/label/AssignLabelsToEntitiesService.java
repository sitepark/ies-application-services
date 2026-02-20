package com.sitepark.ies.application.label;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.MultiEntityAuthorizationService;
import com.sitepark.ies.application.MultiEntityNameResolver;
import com.sitepark.ies.application.audit.AuditBatchLogAction;
import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.label.core.usecase.AssignLabelsToEntitiesRequest;
import com.sitepark.ies.label.core.usecase.AssignLabelsToEntitiesResult;
import com.sitepark.ies.label.core.usecase.AssignLabelsToEntitiesUseCase;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.sharedkernel.security.AccessDeniedException;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

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
public final class AssignLabelsToEntitiesService {

  private final AssignLabelsToEntitiesUseCase assignLabelsToEntitiesUseCase;
  private final MultiEntityAuthorizationService authorizationService;
  private final MultiEntityNameResolver multiEntityNameResolver;
  private final ApplicationAuditLogServiceFactory auditLogServiceFactory;

  @Inject
  AssignLabelsToEntitiesService(
      AssignLabelsToEntitiesUseCase assignLabelsToEntitiesUseCase,
      MultiEntityAuthorizationService entityAccessControlService,
      MultiEntityNameResolver multiEntityNameResolver,
      ApplicationAuditLogServiceFactory auditLogServiceFactory) {
    this.assignLabelsToEntitiesUseCase = assignLabelsToEntitiesUseCase;
    this.multiEntityNameResolver = multiEntityNameResolver;
    this.authorizationService = entityAccessControlService;
    this.auditLogServiceFactory = auditLogServiceFactory;
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
  public int assignLabelsToEntities(@NotNull AssignLabelsToEntitiesServiceRequest request) {

    this.checkAuthorization(request.assignEntitiesToLabelsRequest());

    AssignLabelsToEntitiesResult result =
        this.assignLabelsToEntitiesUseCase.assignEntitiesToLabels(
            request.assignEntitiesToLabelsRequest());

    if (result instanceof AssignLabelsToEntitiesResult.Assigned assigned) {
      this.createAuditLogs(assigned, request.auditParentId());
    }

    return result.assignments().entityRefs().size();
  }

  private void checkAuthorization(AssignLabelsToEntitiesRequest request) {
    for (EntityRef entityRef : request.entityRefs()) {
      if (!this.authorizationService.isWritable(entityRef)) {
        throw new AccessDeniedException(
            "Entity " + entityRef.type() + " with id " + entityRef.id() + " not writable");
      }
    }
  }

  private void createAuditLogs(AssignLabelsToEntitiesResult.Assigned result, String auditParentId) {

    Map<EntityRef, String> entityNames = resolveEntityNames(result);

    var assignments = result.assignments();

    Map<String, ApplicationAuditLogService> auditLogServiceMap =
        this.auditLogServiceFactory.createPerTypeForBatch(
            result.timestamp(),
            auditParentId,
            AuditBatchLogAction.BATCH_ASSIGN_LABELS_TO_ENTITIES,
            assignments.entityRefs());

    assignments
        .entityRefs()
        .forEach(
            entityRef -> {
              List<String> labelIds = assignments.labelIds();
              auditLogServiceMap
                  .get(entityRef.type())
                  .createLog(
                      entityRef,
                      entityNames.get(entityRef),
                      AuditLogAction.ASSIGN_LABELS_TO_ENTITIES,
                      labelIds,
                      labelIds);
            });
  }

  private Map<EntityRef, String> resolveEntityNames(AssignLabelsToEntitiesResult.Assigned result) {
    return multiEntityNameResolver.resolveNames(Set.copyOf(result.assignments().entityRefs()));
  }
}
