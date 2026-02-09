package com.sitepark.ies.application.user;

import com.sitepark.ies.sharedkernel.audit.AuditLogService;
import com.sitepark.ies.sharedkernel.audit.CreateAuditLogEntryFailedException;
import com.sitepark.ies.sharedkernel.audit.CreateAuditLogRequest;
import com.sitepark.ies.sharedkernel.base.Identifier;
import com.sitepark.ies.userrepository.core.domain.value.AuditLogAction;
import com.sitepark.ies.userrepository.core.domain.value.AuditLogEntityType;
import com.sitepark.ies.userrepository.core.usecase.user.RemoveUserRequest;
import com.sitepark.ies.userrepository.core.usecase.user.RemoveUserResult;
import com.sitepark.ies.userrepository.core.usecase.user.RemoveUserUseCase;
import jakarta.inject.Inject;
import java.io.IOException;
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
  private final AuditLogService auditLogService;
  private final Clock clock;

  @Inject
  RemoveUsersService(
      RemoveUserUseCase removeUserUseCase, AuditLogService auditLogService, Clock clock) {
    this.removeUserUseCase = removeUserUseCase;
    this.auditLogService = auditLogService;
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
  public void removeUsers(@NotNull RemoveUsersRequest request) {

    if (request.isEmpty()) {
      return;
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Removing {} user(s)", request.identifiers().size());
    }

    // Create batch parent entry if multiple users
    Instant now = Instant.now(this.clock);
    String parentId =
        request.identifiers().size() > 1
            ? this.createBatchRemoveLog(now, request.auditParentId())
            : request.auditParentId();

    int removedCount = 0;
    int skippedCount = 0;

    // Loop through identifiers and call use case for each
    for (Identifier identifier : request.identifiers()) {
      RemoveUserResult result =
          this.removeUserUseCase.removeUser(
              RemoveUserRequest.builder().identifier(identifier).build());

      // Handle result
      switch (result) {
        case RemoveUserResult.Removed removed -> {
          this.createRemoveAuditLog(removed, parentId);
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

  private String createBatchRemoveLog(Instant timestamp, String auditParentId) {
    return this.auditLogService.createAuditLog(
        new CreateAuditLogRequest(
            AuditLogEntityType.USER.name(),
            null,
            null,
            AuditLogAction.BATCH_REMOVE.name(),
            null,
            null,
            timestamp,
            auditParentId));
  }

  private void createRemoveAuditLog(RemoveUserResult.Removed removed, String parentId) {
    try {
      String backwardData = this.auditLogService.serialize(removed.snapshot());

      CreateAuditLogRequest auditRequest =
          new CreateAuditLogRequest(
              AuditLogEntityType.USER.name(),
              removed.userId(),
              removed.displayName(),
              AuditLogAction.REMOVE.name(),
              backwardData,
              null,
              removed.timestamp(),
              parentId);

      this.auditLogService.createAuditLog(auditRequest);

    } catch (IOException e) {
      throw new CreateAuditLogEntryFailedException(
          AuditLogEntityType.USER.name(), removed.userId(), removed.displayName(), e);
    }
  }
}
