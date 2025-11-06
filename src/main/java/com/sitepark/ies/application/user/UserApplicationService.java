package com.sitepark.ies.application.user;

import com.sitepark.ies.security.core.usecase.password.SetUserPasswordRequest;
import com.sitepark.ies.security.core.usecase.password.SetUserPasswordUseCase;
import com.sitepark.ies.sharedkernel.audit.AuditLogService;
import com.sitepark.ies.sharedkernel.audit.CreateAuditLogRequest;
import com.sitepark.ies.userrepository.core.domain.value.AuditLogAction;
import com.sitepark.ies.userrepository.core.domain.value.AuditLogEntityType;
import com.sitepark.ies.userrepository.core.usecase.user.CreateUserRequest;
import com.sitepark.ies.userrepository.core.usecase.user.CreateUserUseCase;
import com.sitepark.ies.userrepository.core.usecase.user.UpdateUserRequest;
import com.sitepark.ies.userrepository.core.usecase.user.UpdateUserResult;
import com.sitepark.ies.userrepository.core.usecase.user.UpdateUserUseCase;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Application Service that orchestrates User creation across multiple bounded contexts.
 *
 * <p>This service coordinates operations between userrepository-core and security-core, allowing
 * controllers to perform complex multi-step operations without knowing about multiple bounded
 * contexts.
 *
 * <p><b>Responsibilities:</b>
 *
 * <ul>
 *   <li>Orchestrate use cases from different bounded contexts
 *   <li>Manage transactional boundaries across contexts
 *   <li>Provide convenience methods for common workflows
 * </ul>
 *
 * <p><b>Note:</b> This is NOT a domain service. It does not contain business logic. It only
 * coordinates use cases that already contain the business logic.
 */
public final class UserApplicationService {

  private static final Logger LOGGER = LogManager.getLogger();

  private final CreateUserUseCase createUserUseCase;
  private final UpdateUserUseCase updateUserUseCase;
  private final SetUserPasswordUseCase setUserPasswordUseCase;
  private final AuditLogService auditLogService;

  @Inject
  UserApplicationService(
      CreateUserUseCase createUserUseCase,
      UpdateUserUseCase updateUserUseCase,
      SetUserPasswordUseCase setUserPasswordUseCase,
      AuditLogService auditLogService) {
    this.createUserUseCase = createUserUseCase;
    this.updateUserUseCase = updateUserUseCase;
    this.setUserPasswordUseCase = setUserPasswordUseCase;
    this.auditLogService = auditLogService;
  }

  /**
   * Creates a new user with optional password and roles.
   *
   * <p>This is a convenience method that orchestrates:
   *
   * <ol>
   *   <li>User creation (userrepository-core)
   *   <li>Password setting (security-core) - if password provided
   *   <li>Role assignment (userrepository-core) - handled by CreateUserUseCase
   * </ol>
   *
   * @param request contains user data, optional password, and role identifiers
   * @return the created user's ID
   * @throws com.sitepark.ies.sharedkernel.security.AccessDeniedException if user creation is not
   *     allowed
   * @throws com.sitepark.ies.userrepository.core.domain.exception.LoginAlreadyExistsException if
   *     login already exists
   * @throws com.sitepark.ies.sharedkernel.anchor.AnchorAlreadyExistsException if anchor already
   *     exists
   */
  public String createUserWithPassword(@NotNull CreateUserWithPasswordRequest request) {

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(
          "Creating user with login '{}' and {} roles",
          request.user().login(),
          request.roleIdentifiers().size());
    }

    // 1. Create user (without password) via userrepository-core
    String userId =
        this.createUserUseCase.createUser(
            CreateUserRequest.builder()
                .user(request.user())
                .roleIdentifiers(b -> b.identifiers(request.roleIdentifiers()))
                .auditParentId(request.auditParentId())
                .build());

    // 2. Set password (if provided) via security-core
    String password = request.password();
    if (password != null && !password.isBlank()) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Setting password for user '{}'", userId);
      }
      this.setUserPasswordUseCase.setUserPassword(
          SetUserPasswordRequest.builder().userId(userId).newPassword(password).build());
    }

    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Successfully created user '{}' with login '{}'", userId, request.user().login());
    }

    return userId;
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
  public String updateUserWithAudit(@NotNull UpdateUserRequest request) {

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Updating user with ID '{}'", request.user().id());
    }

    // 1. Update user via userrepository-core
    UpdateUserResult result = this.updateUserUseCase.updateUser(request);

    // 2. Create audit log entry only if user was actually updated
    if (result instanceof UpdateUserResult.Updated updated) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("User '{}' was updated, creating audit log entry", updated.userId());
      }

      CreateAuditLogRequest auditRequest =
          new CreateAuditLogRequest(
              AuditLogEntityType.USER.name(),
              updated.userId(),
              updated.displayName(),
              AuditLogAction.UPDATE.name(),
              updated.revertPatch().toJson(),
              updated.patch().toJson(),
              updated.timestamp(),
              request.auditParentId());

      this.auditLogService.createAuditLog(auditRequest);
    } else {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("User '{}' unchanged, no audit log created", result.userId());
      }
    }

    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Successfully processed user update for '{}'", result.userId());
    }

    return result.userId();
  }
}
