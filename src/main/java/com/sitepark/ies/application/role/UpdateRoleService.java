package com.sitepark.ies.application.role;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.MultiEntityNameResolver;
import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.application.label.ReassignLabelsToEntitiesService;
import com.sitepark.ies.application.label.ReassignLabelsToEntitiesServiceRequest;
import com.sitepark.ies.label.core.usecase.ReassignLabelsToEntitiesRequest;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.userrepository.core.domain.entity.Role;
import com.sitepark.ies.userrepository.core.usecase.role.ReassignPrivilegesToRolesResult;
import com.sitepark.ies.userrepository.core.usecase.role.UpdateRoleResult;
import com.sitepark.ies.userrepository.core.usecase.role.UpdateRoleUseCase;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Application Service that orchestrates role update operations with cross-cutting concerns.
 *
 * <p>This service coordinates role updates and associated cross-cutting concerns like audit
 * logging, allowing controllers to perform complete update operations without managing these
 * concerns themselves.
 *
 * <p><b>Responsibilities:</b>
 *
 * <ul>
 *   <li>Update role via userrepository-core
 *   <li>Create audit log entries for changes
 *   <li>Create audit log entries for privilege assignments
 *   <li>Manage transactional boundaries
 * </ul>
 */
public final class UpdateRoleService {

  private static final Logger LOGGER = LogManager.getLogger();

  private final UpdateRoleUseCase updateRoleUseCase;
  private final ReassignLabelsToEntitiesService reassignLabelsToEntitiesService;
  private final MultiEntityNameResolver multiEntityNameResolver;
  private final ApplicationAuditLogServiceFactory auditLogServiceFactory;

  @Inject
  UpdateRoleService(
      UpdateRoleUseCase updateRoleUseCase,
      ReassignLabelsToEntitiesService reassignLabelsToEntitiesService,
      MultiEntityNameResolver multiEntityNameResolver,
      ApplicationAuditLogServiceFactory auditLogServiceFactory) {
    this.updateRoleUseCase = updateRoleUseCase;
    this.reassignLabelsToEntitiesService = reassignLabelsToEntitiesService;
    this.multiEntityNameResolver = multiEntityNameResolver;
    this.auditLogServiceFactory = auditLogServiceFactory;
  }

  /**
   * Updates an existing role and creates an audit log entry.
   *
   * <p>This method orchestrates:
   *
   * <ol>
   *   <li>Role update (userrepository-core)
   *   <li>Audit log creation (audit-core) - only if changes were made
   * </ol>
   *
   * <p>If no changes are detected (role data identical to stored data), the update is skipped and
   * no audit log entry is created.
   *
   * @param request contains role data, privilege identifiers, and optional audit parent ID
   * @return the role ID
   * @throws com.sitepark.ies.sharedkernel.security.AccessDeniedException if role update is not
   *     allowed
   * @throws com.sitepark.ies.userrepository.core.domain.exception.RoleNotFoundException if role
   *     does not exist
   * @throws com.sitepark.ies.sharedkernel.anchor.AnchorAlreadyExistsException if anchor already
   *     exists for a different role
   */
  public String updateRole(@NotNull UpdateRoleServiceRequest request) {

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Updating role with ID '{}'", request.updateRoleRequest().role().id());
    }

    UpdateRoleResult result = this.updateRoleUseCase.updateRole(request.updateRoleRequest());

    this.createAuditLogs(result, request.auditParentId());

    ReassignLabelsToEntitiesServiceRequest labelRequest =
        ReassignLabelsToEntitiesServiceRequest.builder()
            .reassignLabelsToEntitiesRequest(
                ReassignLabelsToEntitiesRequest.builder()
                    .entityRefs(
                        configure -> configure.set(EntityRef.of(Role.class, result.roleId())))
                    .labelIdentifiers(
                        configure -> configure.identifiers(request.labelIdentifiers()))
                    .build())
            .auditParentId(request.auditParentId())
            .build();
    this.reassignLabelsToEntitiesService.reassignEntitiesFromLabels(labelRequest);

    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Successfully processed role update for '{}'", result.roleId());
    }

    return result.roleId();
  }

  protected void createAuditLogs(UpdateRoleResult result, String auditParentId) {
    this.createAuditLogForRoleUpdate(result, auditParentId);
    this.createAuditLogsForPrivilegeReassignment(result, auditParentId);
  }

  private void createAuditLogForRoleUpdate(UpdateRoleResult result, String auditParentId) {

    if (!result.hasRoleChanges()) {
      return;
    }

    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(result.timestamp(), auditParentId);
    var backwardData = result.revertPatch();
    var forwardData = result.patch();
    auditLogService.createLog(
        EntityRef.of(Role.class, result.roleId()),
        result.roleName(),
        AuditLogAction.UPDATE,
        backwardData != null ? backwardData.toJson() : null,
        forwardData != null ? forwardData.toJson() : null);
  }

  private void createAuditLogsForPrivilegeReassignment(
      UpdateRoleResult result, @Nullable String auditParentId) {

    ReassignPrivilegesToRolesResult.Reassigned reassigned =
        result.privilegeReassignmentResult() instanceof ReassignPrivilegesToRolesResult.Reassigned r
            ? r
            : null;
    if (reassigned == null) {
      return;
    }

    String roleName =
        result.hasRoleChanges()
            ? result.roleName()
            : this.multiEntityNameResolver.resolveName(EntityRef.of(Role.class, result.roleId()));

    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(result.timestamp(), auditParentId);

    var assignedPrivilegeIds = reassigned.assignments().privilegeIds();
    if (!assignedPrivilegeIds.isEmpty()) {
      auditLogService.createLog(
          EntityRef.of(Role.class, result.roleId()),
          roleName,
          AuditLogAction.ASSIGN_PRIVILEGES,
          assignedPrivilegeIds,
          assignedPrivilegeIds);
    }

    var unassignedPrivilegeIds = reassigned.unassignments().privilegeIds();
    if (!unassignedPrivilegeIds.isEmpty()) {
      auditLogService.createLog(
          EntityRef.of(Role.class, result.roleId()),
          roleName,
          AuditLogAction.UNASSIGN_PRIVILEGES,
          unassignedPrivilegeIds,
          unassignedPrivilegeIds);
    }
  }
}
