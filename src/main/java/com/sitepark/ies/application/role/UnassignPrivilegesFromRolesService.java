package com.sitepark.ies.application.role;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.MultiEntityNameResolver;
import com.sitepark.ies.application.audit.AuditBatchLogAction;
import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.userrepository.core.domain.entity.Role;
import com.sitepark.ies.userrepository.core.usecase.role.UnassignPrivilegesFromRolesResult;
import com.sitepark.ies.userrepository.core.usecase.role.UnassignPrivilegesFromRolesUseCase;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Application Service that orchestrates privilege unassignment operations with cross-cutting
 * concerns.
 *
 * <p>This service coordinates privilege unassignment from roles and associated cross-cutting
 * concerns like audit logging, allowing controllers to perform complete unassignment operations
 * without managing these concerns themselves.
 *
 * <p><b>Responsibilities:</b>
 *
 * <ul>
 *   <li>Unassign privileges from roles via userrepository-core
 *   <li>Create audit log entries for unassignments
 *   <li>Handle batch operations with parent audit logs
 * </ul>
 */
public final class UnassignPrivilegesFromRolesService {

  private final UnassignPrivilegesFromRolesUseCase unassignPrivilegesFromRolesUseCase;
  private final MultiEntityNameResolver multiEntityNameResolver;
  private final ApplicationAuditLogServiceFactory auditLogServiceFactory;

  @Inject
  UnassignPrivilegesFromRolesService(
      UnassignPrivilegesFromRolesUseCase unassignPrivilegesFromRolesUseCase,
      MultiEntityNameResolver multiEntityNameResolver,
      ApplicationAuditLogServiceFactory auditLogServiceFactory) {
    this.unassignPrivilegesFromRolesUseCase = unassignPrivilegesFromRolesUseCase;
    this.multiEntityNameResolver = multiEntityNameResolver;
    this.auditLogServiceFactory = auditLogServiceFactory;
  }

  /**
   * Unassigns privileges from one or more roles and creates audit log entries.
   *
   * <p>This method orchestrates:
   *
   * <ol>
   *   <li>Privilege unassignment (userrepository-core)
   *   <li>Audit log creation (audit-core) - only for effective unassignments
   *   <li>Batch parent log creation - if multiple roles have unassignments
   * </ol>
   *
   * <p>If no effective unassignments are made (privileges not assigned), no audit logs are created.
   *
   * @param request the unassignment request containing role identifiers, privilege identifiers, and
   *     optional audit parent ID
   * @throws com.sitepark.ies.sharedkernel.security.AccessDeniedException if privilege unassignment
   *     is not allowed
   * @throws com.sitepark.ies.userrepository.core.domain.exception.RoleNotFoundException if a role
   *     does not exist
   * @throws com.sitepark.ies.userrepository.core.domain.exception.PrivilegeNotFoundException if a
   *     privilege does not exist
   */
  public int unassignPrivilegesFromRoles(
      @NotNull UnassignPrivilegesFromRolesServiceRequest request) {

    UnassignPrivilegesFromRolesResult result =
        this.unassignPrivilegesFromRolesUseCase.unassignPrivilegesFromRoles(
            request.unassignPrivilegesFromRolesRequest());

    if (result instanceof UnassignPrivilegesFromRolesResult.Unassigned unassigned) {
      this.createAuditLogs(unassigned, request.auditParentId());
    }

    return result.unassignments().size();
  }

  private void createAuditLogs(
      UnassignPrivilegesFromRolesResult.Unassigned result, @Nullable String auditParentId) {

    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(result.timestamp(), auditParentId);

    Map<String, String> roleNames = resolveRoleNames(result);

    var unassignments = result.unassignments();

    String parentId =
        unassignments.size() > 1
            ? auditLogService.createBatchLog(
                Role.class, AuditBatchLogAction.BATCH_UNASSIGN_PRIVILEGES)
            : auditParentId;
    auditLogService.updateParentId(parentId);

    unassignments
        .roleIds()
        .forEach(
            roleId -> {
              List<String> privilegeIds = unassignments.privilegeIds(roleId);
              auditLogService.createLog(
                  EntityRef.of(Role.class, roleId),
                  roleNames.get(roleId),
                  AuditLogAction.UNASSIGN_PRIVILEGES,
                  privilegeIds,
                  privilegeIds);
            });
  }

  private Map<String, String> resolveRoleNames(
      UnassignPrivilegesFromRolesResult.Unassigned result) {
    return multiEntityNameResolver.resolveRoleNames(Set.copyOf(result.unassignments().roleIds()));
  }
}
