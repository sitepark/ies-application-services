package com.sitepark.ies.application.user;

import com.sitepark.ies.sharedkernel.audit.AuditLogService;
import com.sitepark.ies.sharedkernel.audit.CreateAuditLogEntryFailedException;
import com.sitepark.ies.sharedkernel.audit.CreateAuditLogRequest;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import com.sitepark.ies.userrepository.core.domain.value.AuditLogAction;
import com.sitepark.ies.userrepository.core.domain.value.AuditLogEntityType;
import com.sitepark.ies.userrepository.core.port.UserRepository;
import com.sitepark.ies.userrepository.core.usecase.user.AssignRolesToUsersResult;
import com.sitepark.ies.userrepository.core.usecase.user.AssignRolesToUsersUseCase;
import jakarta.inject.Inject;
import java.io.IOException;
import java.time.Instant;
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
  private final UserRepository userRepository;
  private final AuditLogService auditLogService;

  @Inject
  AssignRolesToUsersService(
      AssignRolesToUsersUseCase assignRolesToUsersUseCase,
      UserRepository userRepository,
      AuditLogService auditLogService) {
    this.assignRolesToUsersUseCase = assignRolesToUsersUseCase;
    this.userRepository = userRepository;
    this.auditLogService = auditLogService;
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
  public void assignRolesToUsers(@NotNull AssignRolesToUsersRequest request) {

    AssignRolesToUsersResult result =
        this.assignRolesToUsersUseCase.assignRolesToUsers(request.toUseCaseRequest());

    if (result instanceof AssignRolesToUsersResult.Assigned assigned) {
      this.createAuditLogs(assigned, request.auditParentId());
    }
  }

  private void createAuditLogs(
      AssignRolesToUsersResult.Assigned assigned, @Nullable String auditParentId) {

    var assignments = assigned.assignments();
    var timestamp = assigned.timestamp();

    String parentId =
        assignments.size() > 1
            ? this.createBatchAssignmentLog(timestamp, auditParentId)
            : auditParentId;

    assignments
        .userIds()
        .forEach(
            userId -> {
              CreateAuditLogRequest createAuditLogRequest =
                  this.buildCreateAuditLogRequest(
                      userId, assignments.roleIds(userId), timestamp, parentId);
              this.auditLogService.createAuditLog(createAuditLogRequest);
            });
  }

  private String createBatchAssignmentLog(Instant timestamp, @Nullable String parentId) {
    return this.auditLogService.createAuditLog(
        new CreateAuditLogRequest(
            AuditLogEntityType.USER.name(),
            null,
            null,
            AuditLogAction.BATCH_ASSIGN_ROLES.name(),
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
          AuditLogEntityType.ROLE.name(), userId, userDisplayName, e);
    }

    return new CreateAuditLogRequest(
        AuditLogEntityType.USER.name(),
        userId,
        userDisplayName,
        AuditLogAction.ASSIGN_ROLES.name(),
        rolesJsonArray,
        rolesJsonArray,
        timestamp,
        parentId);
  }
}
