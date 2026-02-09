package com.sitepark.ies.application.user;

import com.sitepark.ies.security.core.usecase.password.SetUserPasswordRequest;
import com.sitepark.ies.security.core.usecase.password.SetUserPasswordUseCase;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Application Service that orchestrates User creation across multiple bounded contexts.
 *
 * <p>This service coordinates operations between userrepository-core and security-core, allowing
 * controllers to perform complex multi-step user creation operations without knowing about multiple
 * bounded contexts.
 *
 * <p><b>Responsibilities:</b>
 *
 * <ul>
 *   <li>Create user in userrepository-core
 *   <li>Set password in security-core (if provided)
 *   <li>Manage transactional boundaries across contexts
 * </ul>
 */
public final class CreateUserWithPasswordService {

  private static final Logger LOGGER = LogManager.getLogger();

  private final CreateUserService createUserService;
  private final SetUserPasswordUseCase setUserPasswordUseCase;

  @Inject
  CreateUserWithPasswordService(
      CreateUserService createUserService, SetUserPasswordUseCase setUserPasswordUseCase) {
    this.createUserService = createUserService;
    this.setUserPasswordUseCase = setUserPasswordUseCase;
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
        this.createUserService.createUser(
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
}
