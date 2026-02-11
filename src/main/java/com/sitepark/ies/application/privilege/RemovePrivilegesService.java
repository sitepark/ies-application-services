package com.sitepark.ies.application.privilege;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.audit.AuditBatchLogAction;
import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.sharedkernel.base.Identifier;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.userrepository.core.domain.entity.Privilege;
import com.sitepark.ies.userrepository.core.usecase.privilege.RemovePrivilegeRequest;
import com.sitepark.ies.userrepository.core.usecase.privilege.RemovePrivilegeResult;
import com.sitepark.ies.userrepository.core.usecase.privilege.RemovePrivilegeUseCase;
import jakarta.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Application Service that orchestrates privilege removal operations with cross-cutting concerns.
 *
 * <p>This service coordinates privilege removal and associated cross-cutting concerns like audit
 * logging, allowing controllers to perform complete removal operations without managing these
 * concerns themselves.
 *
 * <p><b>Responsibilities:</b>
 *
 * <ul>
 *   <li>Remove privileges via userrepository-core
 *   <li>Create audit log entries for removals
 *   <li>Handle batch operations with parent audit logs
 *   <li>Manage transactional boundaries
 * </ul>
 */
public final class RemovePrivilegesService {

  private static final Logger LOGGER = LogManager.getLogger();

  private final RemovePrivilegeUseCase removePrivilegeUseCase;
  private final ApplicationAuditLogServiceFactory auditLogServiceFactory;
  private final Clock clock;

  @Inject
  RemovePrivilegesService(
      RemovePrivilegeUseCase removePrivilegeUseCase,
      ApplicationAuditLogServiceFactory auditLogServiceFactory,
      Clock clock) {
    this.removePrivilegeUseCase = removePrivilegeUseCase;
    this.auditLogServiceFactory = auditLogServiceFactory;
    this.clock = clock;
  }

  /**
   * Removes one or more privileges and creates audit log entries.
   *
   * <p>This method orchestrates:
   *
   * <ol>
   *   <li>Privilege removal (userrepository-core) - for each identifier
   *   <li>Audit log creation (audit-core) - only for successfully removed privileges
   *   <li>Batch parent log creation - if multiple privileges are being removed
   * </ol>
   *
   * <p>Built-in privileges (e.g., FULL_ACCESS with ID "1") are skipped and logged as warnings.
   *
   * @param request the removal request containing identifiers and optional audit parent ID
   * @throws com.sitepark.ies.sharedkernel.security.AccessDeniedException if privilege removal is
   *     not allowed
   * @throws com.sitepark.ies.userrepository.core.domain.exception.PrivilegeNotFoundException if a
   *     privilege does not exist
   * @throws com.sitepark.ies.sharedkernel.anchor.AnchorNotFoundException if an anchor does not
   *     exist
   */
  public void removePrivileges(@NotNull RemovePrivilegesServiceRequest request) {

    if (request.isEmpty()) {
      return;
    }

    Instant timestamp = Instant.now(this.clock);

    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(timestamp, request.auditParentId());

    String parentId =
        request.identifiers().size() > 1
            ? auditLogService.createBatchLog(Privilege.class, AuditBatchLogAction.BATCH_REMOVE)
            : request.auditParentId();
    auditLogService.updateParentId(parentId);

    int removedCount = 0;
    int skippedCount = 0;

    for (Identifier identifier : request.identifiers()) {
      RemovePrivilegeResult result =
          this.removePrivilegeUseCase.removePrivilege(
              RemovePrivilegeRequest.builder().identifier(identifier).build());

      switch (result) {
        case RemovePrivilegeResult.Removed removed -> {
          auditLogService.createLog(
              EntityRef.of(Privilege.class, removed.privilegeId()),
              removed.privilegeName(),
              AuditLogAction.REMOVE,
              removed.snapshot(),
              null);
          removedCount++;
        }
        case RemovePrivilegeResult.Skipped skipped -> {
          if (LOGGER.isWarnEnabled()) {
            LOGGER.warn(
                "Skipped removal of privilege '{}': {}", skipped.privilegeId(), skipped.reason());
          }
          skippedCount++;
        }
      }
    }

    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Removed {} privilege(s), skipped {}", removedCount, skippedCount);
    }
  }
}
