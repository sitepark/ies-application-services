package com.sitepark.ies.application.user;

import com.sitepark.ies.sharedkernel.audit.AuditLogService;
import com.sitepark.ies.sharedkernel.audit.CreateAuditLogEntryFailedException;
import com.sitepark.ies.sharedkernel.audit.CreateAuditLogRequest;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import com.sitepark.ies.userrepository.core.domain.value.AuditLogAction;
import com.sitepark.ies.userrepository.core.domain.value.AuditLogEntityType;
import com.sitepark.ies.userrepository.core.port.UserRepository;
import com.sitepark.ies.userrepository.core.usecase.user.AssignRolesToUsersResult;
import com.sitepark.ies.userrepository.core.usecase.user.CreateUserResult;
import com.sitepark.ies.userrepository.core.usecase.user.CreateUserUseCase;
import jakarta.inject.Inject;
import java.io.IOException;
import java.time.Instant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Application Service that orchestrates user creation operations with cross-cutting concerns.
 *
 * <p>This service coordinates user creation and associated cross-cutting concerns like audit
 * logging, allowing controllers to perform complete creation operations without managing these
 * concerns themselves.
 *
 * <p><b>Responsibilities:</b>
 *
 * <ul>
 *   <li>Create user via userrepository-core
 *   <li>Create audit log entries for user creation
 *   <li>Create audit log entries for role assignments
 * </ul>
 */
public final class CreateUserService {

  private final CreateUserUseCase createUserUseCase;
  private final UserRepository userRepository;
  private final AuditLogService auditLogService;

  @Inject
  CreateUserService(
      CreateUserUseCase createUserUseCase,
      UserRepository userRepository,
      AuditLogService auditLogService) {
    this.createUserUseCase = createUserUseCase;
    this.userRepository = userRepository;
    this.auditLogService = auditLogService;
  }

  /**
   * Creates a new user and creates audit log entries.
   *
   * <p>This method orchestrates:
   *
   * <ol>
   *   <li>User creation (userrepository-core)
   *   <li>Audit log creation (audit-core) - for user creation
   *   <li>Audit log creation (audit-core) - for role assignments if present
   * </ol>
   *
   * @param request the creation request containing user data, role identifiers, and optional audit
   *     parent ID
   * @return the created user ID
   * @throws com.sitepark.ies.sharedkernel.security.AccessDeniedException if user creation is not
   *     allowed
   * @throws com.sitepark.ies.userrepository.core.domain.exception.LoginAlreadyExistsException if
   *     login already exists
   * @throws com.sitepark.ies.sharedkernel.anchor.AnchorAlreadyExistsException if anchor already
   *     exists
   */
  public String createUser(@NotNull CreateUserRequest request) {

    // 1. Create user via userrepository-core
    CreateUserResult result = this.createUserUseCase.createUser(request.toUseCaseRequest());

    // 2. Create audit log for user creation
    CreateAuditLogRequest userAuditRequest = this.buildUserCreationAuditLogRequest(result);
    this.auditLogService.createAuditLog(userAuditRequest);

    // 3. Create audit logs for role assignments if present
    if (result.roleAssignmentResult() != null
        && result.roleAssignmentResult() instanceof AssignRolesToUsersResult.Assigned assigned) {
      this.createRoleAssignmentAuditLogs(assigned, request.auditParentId());
    }

    return result.userId();
  }

  private CreateAuditLogRequest buildUserCreationAuditLogRequest(CreateUserResult result) {

    String forwardData;
    try {
      forwardData = this.auditLogService.serialize(result.snapshot());
    } catch (IOException e) {
      throw new CreateAuditLogEntryFailedException(
          AuditLogEntityType.USER.name(),
          result.userId(),
          result.snapshot().user().toDisplayName(),
          e);
    }

    return new CreateAuditLogRequest(
        AuditLogEntityType.USER.name(),
        result.userId(),
        result.snapshot().user().toDisplayName(),
        AuditLogAction.CREATE.name(),
        null,
        forwardData,
        result.timestamp(),
        null);
  }

  private void createRoleAssignmentAuditLogs(
      AssignRolesToUsersResult.Assigned assigned, @Nullable String parentId) {

    var assignments = assigned.assignments();
    var timestamp = assigned.timestamp();

    // Create batch parent if multiple users (in practice, CreateUser only has 1 user)
    String auditParentId =
        assignments.size() > 1 ? this.createBatchAssignmentLog(timestamp, parentId) : parentId;

    // Create individual audit logs for each user
    assignments
        .userIds()
        .forEach(
            userId -> {
              CreateAuditLogRequest createAuditLogRequest =
                  this.buildRoleAssignmentAuditLogRequest(
                      userId, assignments.roleIds(userId), timestamp, auditParentId);
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

  private CreateAuditLogRequest buildRoleAssignmentAuditLogRequest(
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
