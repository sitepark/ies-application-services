package com.sitepark.ies.application.user;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.MultiEntityNameResolver;
import com.sitepark.ies.application.audit.AuditBatchLogAction;
import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import com.sitepark.ies.userrepository.core.usecase.user.AssignRolesToUsersResult;
import com.sitepark.ies.userrepository.core.usecase.user.AssignRolesToUsersUseCase;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Application Service that orchestrates role assignment operations with cross-cutting concerns.
 *
 * <p>This service coordinates role assignment to users and associated cross-cutting concerns like
 * audit logging, allowing controllers to perform complete assignment operations without managing
 * these concerns themselves.
 *
 * <p><b>Responsibilities:</b>
 *
 * <ul>
 *   <li>Assign roles to users via userrepository-core
 *   <li>Create audit log entries for assignments
 *   <li>Handle batch operations with parent audit logs
 * </ul>
 */
public final class AssignRolesToUsersService {

  private final AssignRolesToUsersUseCase assignRolesToUsersUseCase;
  private final MultiEntityNameResolver multiEntityNameResolver;
  private final ApplicationAuditLogServiceFactory auditLogServiceFactory;

  @Inject
  AssignRolesToUsersService(
      AssignRolesToUsersUseCase assignRolesToUsersUseCase,
      MultiEntityNameResolver multiEntityNameResolver,
      ApplicationAuditLogServiceFactory auditLogServiceFactory) {
    this.assignRolesToUsersUseCase = assignRolesToUsersUseCase;
    this.multiEntityNameResolver = multiEntityNameResolver;
    this.auditLogServiceFactory = auditLogServiceFactory;
  }

  /**
   * Assigns roles to one or more users and creates audit log entries.
   *
   * <p>This method orchestrates:
   *
   * <ol>
   *   <li>Role assignment (userrepository-core)
   *   <li>Audit log creation (audit-core) - only for effective assignments
   *   <li>Batch parent log creation - if multiple users receive assignments
   * </ol>
   *
   * <p>If no effective assignments are made (all roles already assigned), no audit logs are
   * created.
   *
   * @param request the assignment request containing user identifiers, role identifiers, and
   *     optional audit parent ID
   * @throws com.sitepark.ies.sharedkernel.security.AccessDeniedException if role assignment is not
   *     allowed
   * @throws com.sitepark.ies.userrepository.core.domain.exception.UserNotFoundException if a user
   *     does not exist
   * @throws com.sitepark.ies.userrepository.core.domain.exception.RoleNotFoundException if a role
   *     does not exist
   */
  public void assignRolesToUsers(@NotNull AssignRolesToUsersServiceRequest request) {

    AssignRolesToUsersResult result =
        this.assignRolesToUsersUseCase.assignRolesToUsers(request.assignRolesToUsersRequest());

    if (result instanceof AssignRolesToUsersResult.Assigned assigned) {
      this.createAuditLogs(assigned, request.auditParentId());
    }
  }

  private void createAuditLogs(
      AssignRolesToUsersResult.Assigned result, @Nullable String auditParentId) {

    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(result.timestamp(), auditParentId);

    Map<String, String> userDisplayNames = resolveUserDisplayNames(result);

    var assignments = result.assignments();

    String parentId =
        assignments.size() > 1
            ? auditLogService.createBatchLog(User.class, AuditBatchLogAction.BATCH_ASSIGN_ROLES)
            : auditParentId;
    auditLogService.updateParentId(parentId);

    assignments
        .userIds()
        .forEach(
            userId -> {
              List<String> roleIds = assignments.roleIds(userId);
              auditLogService.createLog(
                  EntityRef.of(User.class, userId),
                  userDisplayNames.get(userId),
                  AuditLogAction.ASSIGN_ROLES,
                  roleIds,
                  roleIds);
            });
  }

  private Map<String, String> resolveUserDisplayNames(AssignRolesToUsersResult.Assigned result) {
    return multiEntityNameResolver.resolveDisplayUserNames(
        Set.copyOf(result.assignments().userIds()));
  }
}
