package com.sitepark.ies.application.privilege;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.MultiEntityNameResolver;
import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.application.label.ReassignLabelsToEntitiesService;
import com.sitepark.ies.application.label.ReassignLabelsToEntitiesServiceRequest;
import com.sitepark.ies.label.core.usecase.ReassignLabelsToEntitiesRequest;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.userrepository.core.domain.entity.Privilege;
import com.sitepark.ies.userrepository.core.usecase.privilege.ReassignRolesToPrivilegesResult;
import com.sitepark.ies.userrepository.core.usecase.privilege.UpdatePrivilegeResult;
import com.sitepark.ies.userrepository.core.usecase.privilege.UpdatePrivilegeUseCase;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Application Service that orchestrates privilege update operations with cross-cutting concerns.
 *
 * <p>This service coordinates privilege updates and associated cross-cutting concerns like audit
 * logging, allowing controllers to perform complete update operations without managing these
 * concerns themselves.
 *
 * <p><b>Responsibilities:</b>
 *
 * <ul>
 *   <li>Update privilege via userrepository-core
 *   <li>Create audit log entries for changes
 *   <li>Create audit log entries for role assignments
 *   <li>Manage transactional boundaries
 * </ul>
 */
public final class UpdatePrivilegeService {

  private static final Logger LOGGER = LogManager.getLogger();

  private final UpdatePrivilegeUseCase updatePrivilegeUseCase;
  private final ReassignLabelsToEntitiesService reassignLabelsToEntitiesService;
  private final MultiEntityNameResolver multiEntityNameResolver;
  private final ApplicationAuditLogServiceFactory auditLogServiceFactory;

  @Inject
  UpdatePrivilegeService(
      UpdatePrivilegeUseCase updatePrivilegeUseCase,
      ReassignLabelsToEntitiesService reassignLabelsToEntitiesService,
      MultiEntityNameResolver multiEntityNameResolver,
      ApplicationAuditLogServiceFactory auditLogServiceFactory) {
    this.updatePrivilegeUseCase = updatePrivilegeUseCase;
    this.reassignLabelsToEntitiesService = reassignLabelsToEntitiesService;
    this.multiEntityNameResolver = multiEntityNameResolver;
    this.auditLogServiceFactory = auditLogServiceFactory;
  }

  /**
   * Updates an existing privilege and creates an audit log entry.
   *
   * <p>This method orchestrates:
   *
   * <ol>
   *   <li>Privilege update (userrepository-core)
   *   <li>Audit log creation (audit-core) - only if changes were made
   * </ol>
   *
   * <p>If no changes are detected (privilege data identical to stored data), the update is skipped
   * and no audit log entry is created.
   *
   * @param request contains privilege data, role identifiers, and optional audit parent ID
   * @return the privilege ID
   * @throws com.sitepark.ies.sharedkernel.security.AccessDeniedException if privilege update is not
   *     allowed
   * @throws com.sitepark.ies.userrepository.core.domain.exception.PrivilegeNotFoundException if
   *     privilege does not exist
   * @throws com.sitepark.ies.sharedkernel.anchor.AnchorAlreadyExistsException if anchor already
   *     exists for a different privilege
   */
  public String updatePrivilege(@NotNull UpdatePrivilegeServiceRequest request) {

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(
          "Updating privilege with ID '{}'", request.updatePrivilegeRequest().privilege().id());
    }

    UpdatePrivilegeResult result =
        this.updatePrivilegeUseCase.updatePrivilege(request.updatePrivilegeRequest());
    this.createAuditLogs(result, request.auditParentId());

    if (request.labelIdentifiers().shouldUpdate()) {
      ReassignLabelsToEntitiesServiceRequest labelRequest =
          ReassignLabelsToEntitiesServiceRequest.builder()
              .reassignLabelsToEntitiesRequest(
                  ReassignLabelsToEntitiesRequest.builder()
                      .entityRefs(
                          configure ->
                              configure.set(EntityRef.of(Privilege.class, result.privilegeId())))
                      .labelIdentifiers(
                          configure -> configure.identifiers(request.labelIdentifiers().getValue()))
                      .build())
              .auditParentId(request.auditParentId())
              .build();
      this.reassignLabelsToEntitiesService.reassignEntitiesFromLabels(labelRequest);
    }

    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Successfully processed privilege update for '{}'", result.privilegeId());
    }

    return result.privilegeId();
  }

  protected void createAuditLogs(UpdatePrivilegeResult result, String auditParentId) {
    this.createAuditLogForPrivilegeUpdate(result, auditParentId);
    this.createAuditLogsForRoleReassignment(result, auditParentId);
  }

  private void createAuditLogForPrivilegeUpdate(
      UpdatePrivilegeResult result, String auditParentId) {

    if (!result.hasPrivilegeChanges()) {
      return;
    }

    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(result.timestamp(), auditParentId);
    var backwardData = result.revertPatch();
    var forwardData = result.patch();
    auditLogService.createLog(
        EntityRef.of(Privilege.class, result.privilegeId()),
        result.privilegeName(),
        AuditLogAction.UPDATE,
        backwardData != null ? backwardData.toJson() : null,
        forwardData != null ? forwardData.toJson() : null);
  }

  private void createAuditLogsForRoleReassignment(
      UpdatePrivilegeResult result, String auditParentId) {

    ReassignRolesToPrivilegesResult.Reassigned reassigned =
        result.roleReassignmentResult() instanceof ReassignRolesToPrivilegesResult.Reassigned r
            ? r
            : null;
    if (reassigned == null) {
      return;
    }

    String privilegeName =
        result.hasPrivilegeChanges()
            ? result.privilegeName()
            : this.multiEntityNameResolver.resolveName(
                EntityRef.of(Privilege.class, result.privilegeId()));

    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(result.timestamp(), auditParentId);

    var assignedRoleIds = reassigned.assignments().roleIds();
    if (!assignedRoleIds.isEmpty()) {
      auditLogService.createLog(
          EntityRef.of(Privilege.class, result.privilegeId()),
          privilegeName,
          AuditLogAction.ASSIGN_ROLES,
          assignedRoleIds,
          assignedRoleIds);
    }

    var unassignedRoleIds = reassigned.unassignments().roleIds();
    if (!unassignedRoleIds.isEmpty()) {
      auditLogService.createLog(
          EntityRef.of(Privilege.class, result.privilegeId()),
          privilegeName,
          AuditLogAction.UNASSIGN_ROLES,
          unassignedRoleIds,
          unassignedRoleIds);
    }
  }
}
