package com.sitepark.ies.application.user;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.audit.AuditBatchLogAction;
import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.sharedkernel.base.Identifier;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import com.sitepark.ies.userrepository.core.usecase.user.RemoveUserRequest;
import com.sitepark.ies.userrepository.core.usecase.user.RemoveUserResult;
import com.sitepark.ies.userrepository.core.usecase.user.RemoveUserUseCase;
import jakarta.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Application Service that orchestrates user removal operations with cross-cutting concerns.
 *
 * <p>This service coordinates user removal and associated cross-cutting concerns like audit
 * logging, allowing controllers to perform complete removal operations without managing these
 * concerns themselves.
 *
 * <p><b>Responsibilities:</b>
 *
 * <ul>
 *   <li>Remove users via userrepository-core
 *   <li>Create audit log entries for removals
 *   <li>Handle batch operations with parent audit logs
 *   <li>Manage transactional boundaries
 * </ul>
 */
public final class RemoveUsersService {

  private static final Logger LOGGER = LogManager.getLogger();

  private final RemoveUserUseCase removeUserUseCase;
  private final ApplicationAuditLogServiceFactory auditLogServiceFactory;
  private final Clock clock;

  @Inject
  RemoveUsersService(
      RemoveUserUseCase removeUserUseCase,
      ApplicationAuditLogServiceFactory auditLogServiceFactory,
      Clock clock) {
    this.removeUserUseCase = removeUserUseCase;
    this.auditLogServiceFactory = auditLogServiceFactory;
    this.clock = clock;
  }

  /**
   * Removes one or more users and creates audit log entries.
   *
   * <p>This method orchestrates:
   *
   * <ol>
   *   <li>User removal (userrepository-core) - for each identifier
   *   <li>Audit log creation (audit-core) - only for successfully removed users
   *   <li>Batch parent log creation - if multiple users are being removed
   * </ol>
   *
   * <p>Built-in users (e.g., Administrator with ID "1") are skipped and logged as warnings.
   *
   * @param request the removal request containing identifiers and optional audit parent ID
   * @throws com.sitepark.ies.sharedkernel.security.AccessDeniedException if user removal is not
   *     allowed
   * @throws com.sitepark.ies.userrepository.core.domain.exception.UserNotFoundException if a user
   *     does not exist
   * @throws com.sitepark.ies.sharedkernel.anchor.AnchorNotFoundException if an anchor does not
   *     exist
   */
  public void removeUsers(@NotNull RemoveUsersServiceRequest request) {

    if (request.isEmpty()) {
      return;
    }

    Instant timestamp = Instant.now(this.clock);

    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(timestamp, request.auditParentId());

    String parentId =
        request.identifiers().size() > 1
            ? auditLogService.createBatchLog(User.class, AuditBatchLogAction.BATCH_ASSIGN_ROLES)
            : request.auditParentId();
    auditLogService.updateParentId(parentId);

    int removedCount = 0;
    int skippedCount = 0;

    for (Identifier identifier : request.identifiers()) {
      RemoveUserResult result =
          this.removeUserUseCase.removeUser(
              RemoveUserRequest.builder().identifier(identifier).build());

      switch (result) {
        case RemoveUserResult.Removed removed -> {
          auditLogService.createLog(
              EntityRef.of(User.class, removed.userId()),
              removed.displayName(),
              AuditLogAction.REMOVE,
              removed.snapshot(),
              null);
          removedCount++;
        }
        case RemoveUserResult.Skipped skipped -> {
          if (LOGGER.isWarnEnabled()) {
            LOGGER.warn("Skipped removal of user '{}': {}", skipped.userId(), skipped.reason());
          }
          skippedCount++;
        }
      }
    }

    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Removed {} user(s), skipped {}", removedCount, skippedCount);
    }
  }
}
