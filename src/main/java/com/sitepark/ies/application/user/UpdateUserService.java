package com.sitepark.ies.application.user;

import com.sitepark.ies.sharedkernel.audit.AuditLogService;
import com.sitepark.ies.sharedkernel.audit.CreateAuditLogEntryFailedException;
import com.sitepark.ies.sharedkernel.audit.CreateAuditLogRequest;
import com.sitepark.ies.userrepository.core.domain.value.AuditLogAction;
import com.sitepark.ies.userrepository.core.domain.value.AuditLogEntityType;
import com.sitepark.ies.userrepository.core.usecase.user.ReassignRolesToUsersResult;
import com.sitepark.ies.userrepository.core.usecase.user.UpdateUserResult;
import com.sitepark.ies.userrepository.core.usecase.user.UpdateUserUseCase;
import com.sitepark.ies.userrepository.core.usecase.user.UserUpdateResult;
import jakarta.inject.Inject;
import java.io.IOException;
import java.time.Instant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Application Service that orchestrates user update operations with cross-cutting concerns.
 *
 * <p>This service coordinates user updates and associated cross-cutting concerns like audit
 * logging, allowing controllers to perform complete update operations without managing these
 * concerns themselves.
 *
 * <p><b>Responsibilities:</b>
 *
 * <ul>
 *   <li>Update user via userrepository-core
 *   <li>Create audit log entries for changes
 *   <li>Create audit log entries for role assignments
 *   <li>Manage transactional boundaries
 * </ul>
 */
public final class UpdateUserService {

  private static final Logger LOGGER = LogManager.getLogger();

  private final UpdateUserUseCase updateUserUseCase;
  private final AuditLogService auditLogService;

  @Inject
  UpdateUserService(UpdateUserUseCase updateUserUseCase, AuditLogService auditLogService) {
    this.updateUserUseCase = updateUserUseCase;
    this.auditLogService = auditLogService;
  }

  /**
   * Updates an existing user and creates an audit log entry.
   *
   * <p>This method orchestrates:
   *
   * <ol>
   *   <li>User update (userrepository-core)
   *   <li>Audit log creation (audit-core) - only if changes were made
   * </ol>
   *
   * <p>If no changes are detected (user data identical to stored data), the update is skipped and
   * no audit log entry is created.
   *
   * @param request contains user data, role identifiers, and optional audit parent ID
   * @return the user ID
   * @throws com.sitepark.ies.sharedkernel.security.AccessDeniedException if user update is not
   *     allowed
   * @throws com.sitepark.ies.userrepository.core.domain.exception.UserNotFoundException if user
   *     does not exist
   * @throws com.sitepark.ies.userrepository.core.domain.exception.LoginAlreadyExistsException if
   *     login already exists for a different user
   * @throws com.sitepark.ies.sharedkernel.anchor.AnchorAlreadyExistsException if anchor already
   *     exists for a different user
   */
  public String updateUser(@NotNull UpdateUserRequest request) {

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Updating user with ID '{}'", request.user().id());
    }

    UpdateUserResult result = this.updateUserUseCase.updateUser(request.toUseCaseRequest());

    this.createAuditLogForUserUpdate(request, result);
    this.createAuditLogsForRoleReassignment(request, result);

    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Successfully processed user update for '{}'", result.userId());
    }

    return result.userId();
  }

  private void createAuditLogForUserUpdate(UpdateUserRequest request, UpdateUserResult result) {

    UserUpdateResult.Updated updated = result.getUserUpdate();
    if (updated == null) {
      return;
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("User '{}' was updated, creating audit log entries", result.userId());
    }

    CreateAuditLogRequest auditRequest =
        new CreateAuditLogRequest(
            AuditLogEntityType.USER.name(),
            result.userId(),
            updated.displayName(),
            AuditLogAction.UPDATE.name(),
            updated.revertPatch().toJson(),
            updated.patch().toJson(),
            result.timestamp(),
            request.auditParentId());

    this.auditLogService.createAuditLog(auditRequest);
  }

  private void createAuditLogsForRoleReassignment(
      UpdateUserRequest request, UpdateUserResult result) {
    ReassignRolesToUsersResult.Reassigned reassigned = result.getRoleReassignment();
    if (reassigned == null) {
      return;
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Creating audit logs for role assignments for user '{}'", result.userId());
    }
    this.createRoleReassignmentAuditLogs(
        result.userId(), request.user().toDisplayName(), reassigned, request.auditParentId());
  }

  private void createRoleReassignmentAuditLogs(
      String userId,
      String userDisplayName,
      ReassignRolesToUsersResult.Reassigned reassigned,
      @Nullable String parentId) {

    var assignments = reassigned.assignments();
    var unassignment = reassigned.unassignments();
    var timestamp = reassigned.timestamp();

    String auditParentId =
        (assignments.size() + unassignment.size()) > 1
            ? this.createBatchReassignmentLog(timestamp, parentId)
            : parentId;

    if (!assignments.isEmpty()) {
      CreateAuditLogRequest createAuditLogRequest =
          this.buildCreateAuditLogRequest(
              AuditLogAction.ASSIGN_ROLES,
              userId,
              userDisplayName,
              assignments.roleIds(userId),
              timestamp,
              auditParentId);
      this.auditLogService.createAuditLog(createAuditLogRequest);
    }

    if (!unassignment.isEmpty()) {
      CreateAuditLogRequest createAuditLogRequest =
          this.buildCreateAuditLogRequest(
              AuditLogAction.UNASSIGN_ROLES,
              userId,
              userDisplayName,
              unassignment.roleIds(userId),
              timestamp,
              auditParentId);
      this.auditLogService.createAuditLog(createAuditLogRequest);
    }
  }

  private String createBatchReassignmentLog(Instant timestamp, @Nullable String parentId) {
    return this.auditLogService.createAuditLog(
        new CreateAuditLogRequest(
            AuditLogEntityType.USER.name(),
            null,
            null,
            AuditLogAction.BATCH_REASSIGN_ROLES.name(),
            null,
            null,
            timestamp,
            parentId));
  }

  private CreateAuditLogRequest buildCreateAuditLogRequest(
      AuditLogAction action,
      String userId,
      String userDisplayName,
      java.util.List<String> roleIds,
      Instant timestamp,
      @Nullable String parentId) {

    String rolesJsonArray;
    try {
      rolesJsonArray = this.auditLogService.serialize(roleIds);
    } catch (IOException e) {
      throw new CreateAuditLogEntryFailedException(
          AuditLogEntityType.ROLE.name(), userId, userDisplayName, e);
    }

    return new CreateAuditLogRequest(
        AuditLogEntityType.USER.name(),
        userId,
        userDisplayName,
        action.name(),
        rolesJsonArray,
        rolesJsonArray,
        timestamp,
        parentId);
  }
}
