package com.sitepark.ies.application.user;

import com.sitepark.ies.sharedkernel.audit.AuditLogService;
import com.sitepark.ies.sharedkernel.audit.CreateAuditLogEntryFailedException;
import com.sitepark.ies.sharedkernel.audit.CreateAuditLogRequest;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import com.sitepark.ies.userrepository.core.domain.value.AuditLogAction;
import com.sitepark.ies.userrepository.core.domain.value.AuditLogEntityType;
import com.sitepark.ies.userrepository.core.port.UserRepository;
import com.sitepark.ies.userrepository.core.usecase.user.UnassignRolesFromUsersRequest;
import com.sitepark.ies.userrepository.core.usecase.user.UnassignRolesFromUsersResult;
import com.sitepark.ies.userrepository.core.usecase.user.UnassignRolesFromUsersUseCase;
import jakarta.inject.Inject;
import java.io.IOException;
import java.time.Instant;
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
  private final UserRepository userRepository;
  private final AuditLogService auditLogService;

  @Inject
  UnassignRolesFromUsersService(
      UnassignRolesFromUsersUseCase unassignRolesFromUsersUseCase,
      UserRepository userRepository,
      AuditLogService auditLogService) {
    this.unassignRolesFromUsersUseCase = unassignRolesFromUsersUseCase;
    this.userRepository = userRepository;
    this.auditLogService = auditLogService;
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
  public void unassignRolesFromUsers(@NotNull UnassignRolesFromUsersRequest request) {

    UnassignRolesFromUsersResult result =
        this.unassignRolesFromUsersUseCase.unassignRolesFromUsers(request);

    if (result instanceof UnassignRolesFromUsersResult.Unassigned unassigned) {
      this.createAuditLogs(unassigned, request.auditParentId());
    }
  }

  private void createAuditLogs(
      UnassignRolesFromUsersResult.Unassigned unassigned, @Nullable String auditParentId) {

    var unassignments = unassigned.unassignments();
    var timestamp = unassigned.timestamp();

    String parentId =
        unassignments.size() > 1
            ? this.createBatchUnassignmentLog(timestamp, auditParentId)
            : auditParentId;

    unassignments
        .userIds()
        .forEach(
            userId -> {
              CreateAuditLogRequest createAuditLogRequest =
                  this.buildCreateAuditLogRequest(
                      userId, unassignments.roleIds(userId), timestamp, parentId);
              this.auditLogService.createAuditLog(createAuditLogRequest);
            });
  }

  private String createBatchUnassignmentLog(Instant timestamp, @Nullable String parentId) {
    return this.auditLogService.createAuditLog(
        new CreateAuditLogRequest(
            AuditLogEntityType.USER.name(),
            null,
            null,
            AuditLogAction.BATCH_UNASSIGN_ROLES.name(),
            null,
            null,
            timestamp,
            parentId));
  }

  private CreateAuditLogRequest buildCreateAuditLogRequest(
      String userId, java.util.List<String> roleIds, Instant timestamp, @Nullable String parentId) {

    String userDisplayName = this.userRepository.get(userId).map(User::toDisplayName).orElse(null);

    String rolesJsonArray;
    try {
      rolesJsonArray = this.auditLogService.serialize(roleIds);
    } catch (IOException e) {
      throw new CreateAuditLogEntryFailedException(
          AuditLogEntityType.USER.name(), userId, userDisplayName, e);
    }

    return new CreateAuditLogRequest(
        AuditLogEntityType.USER.name(),
        userId,
        userDisplayName,
        AuditLogAction.UNASSIGN_ROLES.name(),
        rolesJsonArray,
        rolesJsonArray,
        timestamp,
        parentId);
  }
}
