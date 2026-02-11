package com.sitepark.ies.application.label;

import static java.util.stream.Stream.concat;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.MultiEntityAuthorizationService;
import com.sitepark.ies.application.MultiEntityNameResolver;
import com.sitepark.ies.application.audit.AuditBatchLogAction;
import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.label.core.usecase.ReassignLabelsToEntitiesRequest;
import com.sitepark.ies.label.core.usecase.ReassignLabelsToEntitiesResult;
import com.sitepark.ies.label.core.usecase.ReassignLabelsToEntitiesUseCase;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.sharedkernel.security.AccessDeniedException;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

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
  private final ApplicationAuditLogServiceFactory auditLogServiceFactory;

  @Inject
  ReassignLabelsToEntitiesService(
      ReassignLabelsToEntitiesUseCase reassignLabelsToEntitiesUseCase,
      MultiEntityAuthorizationService authorizationService,
      MultiEntityNameResolver multiEntityNameResolver,
      ApplicationAuditLogServiceFactory auditLogServiceFactory) {
    this.reassignLabelsToEntitiesUseCase = reassignLabelsToEntitiesUseCase;
    this.authorizationService = authorizationService;
    this.multiEntityNameResolver = multiEntityNameResolver;
    this.auditLogServiceFactory = auditLogServiceFactory;
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
  public int reassignEntitiesFromLabels(@NotNull ReassignLabelsToEntitiesServiceRequest request) {

    this.checkAuthorization(request.reassignLabelsToEntitiesRequest());

    ReassignLabelsToEntitiesResult result =
        this.reassignLabelsToEntitiesUseCase.reassignLabelsToEntities(
            request.reassignLabelsToEntitiesRequest());

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
      ReassignLabelsToEntitiesServiceRequest request,
      ReassignLabelsToEntitiesResult.Reassigned result) {

    Map<EntityRef, String> entityNames = resolveEntityNames(result);

    var unassignments = result.unassignments();
    var assignments = result.assignments();

    Map<String, ApplicationAuditLogService> auditLogServiceMap =
        this.createAuditLogServicePerType(result, request.auditParentId());

    unassignments
        .entityRefs()
        .forEach(
            entityRef -> {
              List<String> labels = unassignments.labelIds(entityRef);
              auditLogServiceMap
                  .get(entityRef.type())
                  .createLog(
                      entityRef,
                      entityNames.get(entityRef),
                      AuditLogAction.UNASSIGN_ENTITIES_FROM_LABEL,
                      labels,
                      labels);
            });

    assignments
        .entityRefs()
        .forEach(
            entityRef -> {
              List<String> labels = unassignments.labelIds(entityRef);
              auditLogServiceMap
                  .get(entityRef.type())
                  .createLog(
                      entityRef,
                      entityNames.get(entityRef),
                      AuditLogAction.ASSIGN_ENTITIES_TO_LABEL,
                      labels,
                      labels);
            });
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

  private Map<String, ApplicationAuditLogService> createAuditLogServicePerType(
      ReassignLabelsToEntitiesResult.Reassigned result, String auditParentId) {
    Set<String> entityTypes =
        concat(
                result.assignments().entityRefs().stream(),
                result.unassignments().entityRefs().stream())
            .map(EntityRef::type)
            .collect(Collectors.toSet());

    return this.auditLogServiceFactory.createForBatchPerType(
        result.timestamp(),
        auditParentId,
        AuditBatchLogAction.BATCH_REASSIGN_LABELS_TO_ENTITIES,
        entityTypes);
  }
}
