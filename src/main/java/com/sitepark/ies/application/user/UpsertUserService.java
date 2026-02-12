package com.sitepark.ies.application.user;

import com.sitepark.ies.application.value.UpsertResult;
import com.sitepark.ies.userrepository.core.usecase.user.UpsertUserResult;
import com.sitepark.ies.userrepository.core.usecase.user.UpsertUserUseCase;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;

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
 *   <li>Update user via user-core
 *   <li>Create audit log entries for changes (only if changes were made)
 * </ul>
 */
public final class UpsertUserService {

  private final UpsertUserUseCase upsertUserUseCase;
  private final CreateUserService createUserService;
  private final UpdateUserService updateUserService;

  @Inject
  UpsertUserService(
      UpsertUserUseCase upsertUserUseCase,
      CreateUserService createUserService,
      UpdateUserService updateUserService) {
    this.upsertUserUseCase = upsertUserUseCase;
    this.createUserService = createUserService;
    this.updateUserService = updateUserService;
  }

  public UpsertResult upsertUser(@NotNull UpsertUserServiceRequest request) {

    UpsertUserResult result = this.upsertUserUseCase.upsertUser(request.upsertUserRequest());

    if (result instanceof UpsertUserResult.Updated updated) {
      if (!updated.updateUserResult().hasAnyChanges()) {
        return UpsertResult.updated(false);
      }
      this.updateUserService.createAuditLogs(updated.updateUserResult(), request.auditParentId());
      return UpsertResult.updated(true);
    } else if (result instanceof UpsertUserResult.Created created) {
      this.createUserService.createAuditLogs(created.createUserResult(), request.auditParentId());
      return UpsertResult.created(created.userId());
    }

    return UpsertResult.updated(false);
  }
}
