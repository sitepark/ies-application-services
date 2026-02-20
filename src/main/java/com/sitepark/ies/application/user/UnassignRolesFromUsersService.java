package com.sitepark.ies.application.user;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.MultiEntityNameResolver;
import com.sitepark.ies.application.audit.AuditBatchLogAction;
import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import com.sitepark.ies.userrepository.core.usecase.user.UnassignRolesFromUsersResult;
import com.sitepark.ies.userrepository.core.usecase.user.UnassignRolesFromUsersUseCase;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Application Service that orchestrates role unassignment operations with cross-cutting concerns.
 *
 * <p>This service coordinates role unassignment from users and associated cross-cutting concerns
 * like audit logging, allowing controllers to perform complete unassignment operations without
 * managing these concerns themselves.
 *
 * <p><b>Responsibilities:</b>
 *
 * <ul>
 *   <li>Unassign roles from users via userrepository-core
 *   <li>Create audit log entries for unassignments
 *   <li>Handle batch operations with parent audit logs
 * </ul>
 */
public final class UnassignRolesFromUsersService {

  private final UnassignRolesFromUsersUseCase unassignRolesFromUsersUseCase;
  private final MultiEntityNameResolver multiEntityNameResolver;
  private final ApplicationAuditLogServiceFactory auditLogServiceFactory;

  @Inject
  UnassignRolesFromUsersService(
      UnassignRolesFromUsersUseCase unassignRolesFromUsersUseCase,
      MultiEntityNameResolver multiEntityNameResolver,
      ApplicationAuditLogServiceFactory auditLogServiceFactory) {
    this.unassignRolesFromUsersUseCase = unassignRolesFromUsersUseCase;
    this.multiEntityNameResolver = multiEntityNameResolver;
    this.auditLogServiceFactory = auditLogServiceFactory;
  }

  /**
   * Unassigns roles from one or more users and creates audit log entries.
   *
   * <p>This method orchestrates:
   *
   * <ol>
   *   <li>Role unassignment (userrepository-core)
   *   <li>Audit log creation (audit-core) - only for effective unassignments
   *   <li>Batch parent log creation - if multiple users have unassignments
   * </ol>
   *
   * <p>If no effective unassignments are made (roles not assigned), no audit logs are created.
   *
   * @param request the unassignment request containing user identifiers, role identifiers, and
   *     optional audit parent ID
   * @throws com.sitepark.ies.sharedkernel.security.AccessDeniedException if role unassignment is
   *     not allowed
   * @throws com.sitepark.ies.userrepository.core.domain.exception.UserNotFoundException if a user
   *     does not exist
   * @throws com.sitepark.ies.userrepository.core.domain.exception.RoleNotFoundException if a role
   *     does not exist
   */
  public int unassignRolesFromUsers(@NotNull UnassignRolesFromUsersServiceRequest request) {

    UnassignRolesFromUsersResult result =
        this.unassignRolesFromUsersUseCase.unassignRolesFromUsers(
            request.unassignRolesFromUsersRequest());

    if (result instanceof UnassignRolesFromUsersResult.Unassigned unassigned) {
      this.createAuditLogs(unassigned, request.auditParentId());
    }

    return result.unassignments().size();
  }

  private void createAuditLogs(
      UnassignRolesFromUsersResult.Unassigned result, @Nullable String auditParentId) {

    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(result.timestamp(), auditParentId);

    Map<String, String> userDisplayNames = resolveUserDisplayNames(result);

    var unassignments = result.unassignments();

    String parentId =
        unassignments.size() > 1
            ? auditLogService.createBatchLog(User.class, AuditBatchLogAction.BATCH_UNASSIGN_ROLES)
            : auditParentId;
    auditLogService.updateParentId(parentId);

    unassignments
        .userIds()
        .forEach(
            userId -> {
              List<String> roleIds = unassignments.roleIds(userId);
              auditLogService.createLog(
                  EntityRef.of(User.class, userId),
                  userDisplayNames.get(userId),
                  AuditLogAction.UNASSIGN_ROLES,
                  roleIds,
                  roleIds);
            });
  }

  private Map<String, String> resolveUserDisplayNames(
      UnassignRolesFromUsersResult.Unassigned result) {
    return multiEntityNameResolver.resolveDisplayUserNames(
        Set.copyOf(result.unassignments().userIds()));
  }
}
