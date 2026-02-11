package com.sitepark.ies.application.label;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.MultiEntityAuthorizationService;
import com.sitepark.ies.application.MultiEntityNameResolver;
import com.sitepark.ies.application.audit.AuditBatchLogAction;
import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.label.core.usecase.UnassignEntitiesFromLabelsRequest;
import com.sitepark.ies.label.core.usecase.UnassignEntitiesFromLabelsUseCase;
import com.sitepark.ies.label.core.usecase.UnassignEntitiesToLabelsResult;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.sharedkernel.security.AccessDeniedException;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

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
  private final MultiEntityNameResolver multiEntityNameResolver;
  private final ApplicationAuditLogServiceFactory auditLogServiceFactory;

  @Inject
  UnassignEntitiesFromLabelsService(
      UnassignEntitiesFromLabelsUseCase unassignEntitiesFromLabelsUseCase,
      MultiEntityAuthorizationService authorizationService,
      MultiEntityNameResolver multiEntityNameResolver,
      ApplicationAuditLogServiceFactory auditLogServiceFactory) {
    this.unassignEntitiesFromLabelsUseCase = unassignEntitiesFromLabelsUseCase;
    this.authorizationService = authorizationService;
    this.multiEntityNameResolver = multiEntityNameResolver;
    this.auditLogServiceFactory = auditLogServiceFactory;
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
  public int unassignEntitiesFromLabels(@NotNull UnassignEntitiesFromLabelsServiceRequest request) {

    this.checkAuthorization(request.unassignEntitiesFromLabelsRequest());

    UnassignEntitiesToLabelsResult result =
        this.unassignEntitiesFromLabelsUseCase.unassignEntitiesFromLabels(
            request.unassignEntitiesFromLabelsRequest());

    if (result instanceof UnassignEntitiesToLabelsResult.Unassigned unassigned) {
      this.createAuditLogs(unassigned, request.auditParentId());
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

  private void createAuditLogs(
      UnassignEntitiesToLabelsResult.Unassigned result, String auditParentId) {

    Map<EntityRef, String> entityNames = resolveEntityNames(result);

    var unassignments = result.unassignments();

    Map<String, ApplicationAuditLogService> auditLogServiceMap =
        this.auditLogServiceFactory.createForBatchPerType(
            result.timestamp(),
            auditParentId,
            AuditBatchLogAction.BATCH_UNASSIGN_ENTITIES_FROM_LABEL,
            unassignments.entityRefs().stream().map(EntityRef::type).collect(Collectors.toSet()));

    unassignments
        .entityRefs()
        .forEach(
            entityRef -> {
              List<String> labels = unassignments.labelIds();
              auditLogServiceMap
                  .get(entityRef.type())
                  .createLog(
                      entityRef,
                      entityNames.get(entityRef),
                      AuditLogAction.UNASSIGN_ENTITIES_FROM_LABEL,
                      labels,
                      labels);
            });
  }

  private Map<EntityRef, String> resolveEntityNames(
      UnassignEntitiesToLabelsResult.Unassigned result) {
    return multiEntityNameResolver.resolveNames(Set.copyOf(result.unassignments().entityRefs()));
  }
}
