package com.sitepark.ies.application.role;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.MultiEntityNameResolver;
import com.sitepark.ies.application.audit.AuditBatchLogAction;
import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.userrepository.core.domain.entity.Role;
import com.sitepark.ies.userrepository.core.usecase.role.AssignPrivilegesToRolesResult;
import com.sitepark.ies.userrepository.core.usecase.role.AssignPrivilegesToRolesUseCase;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Application Service that orchestrates privilege assignment operations with cross-cutting
 * concerns.
 *
 * <p>This service coordinates privilege assignment to roles and associated cross-cutting concerns
 * like audit logging, allowing controllers to perform complete assignment operations without
 * managing these concerns themselves.
 *
 * <p><b>Responsibilities:</b>
 *
 * <ul>
 *   <li>Assign privileges to roles via userrepository-core
 *   <li>Create audit log entries for assignments
 *   <li>Handle batch operations with parent audit logs
 * </ul>
 */
public final class AssignPrivilegesToRolesService {

  private final AssignPrivilegesToRolesUseCase assignPrivilegesToRolesUseCase;
  private final MultiEntityNameResolver multiEntityNameResolver;
  private final ApplicationAuditLogServiceFactory auditLogServiceFactory;

  @Inject
  AssignPrivilegesToRolesService(
      AssignPrivilegesToRolesUseCase assignPrivilegesToRolesUseCase,
      MultiEntityNameResolver multiEntityNameResolver,
      ApplicationAuditLogServiceFactory auditLogServiceFactory) {
    this.assignPrivilegesToRolesUseCase = assignPrivilegesToRolesUseCase;
    this.multiEntityNameResolver = multiEntityNameResolver;
    this.auditLogServiceFactory = auditLogServiceFactory;
  }

  /**
   * Assigns privileges to one or more roles and creates audit log entries.
   *
   * <p>This method orchestrates:
   *
   * <ol>
   *   <li>Privilege assignment (userrepository-core)
   *   <li>Audit log creation (audit-core) - only for effective assignments
   *   <li>Batch parent log creation - if multiple roles receive assignments
   * </ol>
   *
   * <p>If no effective assignments are made (all privileges already assigned), no audit logs are
   * created.
   *
   * @param request the assignment request containing role identifiers, privilege identifiers, and
   *     optional audit parent ID
   * @throws com.sitepark.ies.sharedkernel.security.AccessDeniedException if privilege assignment is
   *     not allowed
   * @throws com.sitepark.ies.userrepository.core.domain.exception.RoleNotFoundException if a role
   *     does not exist
   * @throws com.sitepark.ies.userrepository.core.domain.exception.PrivilegeNotFoundException if a
   *     privilege does not exist
   */
  public void assignPrivilegesToRoles(@NotNull AssignPrivilegesToRolesServiceRequest request) {

    AssignPrivilegesToRolesResult result =
        this.assignPrivilegesToRolesUseCase.assignPrivilegesToRoles(
            request.assignPrivilegesToRolesRequest());

    if (result instanceof AssignPrivilegesToRolesResult.Assigned assigned) {
      this.createAuditLogs(assigned, request.auditParentId());
    }
  }

  private void createAuditLogs(
      AssignPrivilegesToRolesResult.Assigned result, @Nullable String auditParentId) {

    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(result.timestamp(), auditParentId);

    Map<String, String> roleNames = resolveRoleNames(result);

    var assignments = result.assignments();

    String parentId =
        assignments.size() > 1
            ? auditLogService.createBatchLog(
                Role.class, AuditBatchLogAction.BATCH_ASSIGN_PRIVILEGES)
            : auditParentId;
    auditLogService.updateParentId(parentId);

    assignments
        .roleIds()
        .forEach(
            roleId -> {
              List<String> privilegeIds = assignments.privilegeIds(roleId);
              auditLogService.createLog(
                  EntityRef.of(Role.class, roleId),
                  roleNames.get(roleId),
                  AuditLogAction.ASSIGN_PRIVILEGES,
                  privilegeIds,
                  privilegeIds);
            });
  }

  private Map<String, String> resolveRoleNames(AssignPrivilegesToRolesResult.Assigned result) {
    return multiEntityNameResolver.resolveRoleNames(Set.copyOf(result.assignments().roleIds()));
  }
}
