package com.sitepark.ies.application.role;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.audit.AuditBatchLogAction;
import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.sharedkernel.base.Identifier;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.userrepository.core.domain.entity.Role;
import com.sitepark.ies.userrepository.core.usecase.role.RemoveRoleRequest;
import com.sitepark.ies.userrepository.core.usecase.role.RemoveRoleResult;
import com.sitepark.ies.userrepository.core.usecase.role.RemoveRoleUseCase;
import jakarta.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Application Service that orchestrates role removal operations with cross-cutting concerns.
 *
 * <p>This service coordinates role removal and associated cross-cutting concerns like audit
 * logging, allowing controllers to perform complete removal operations without managing these
 * concerns themselves.
 *
 * <p><b>Responsibilities:</b>
 *
 * <ul>
 *   <li>Remove roles via userrepository-core
 *   <li>Create audit log entries for removals
 *   <li>Handle batch operations with parent audit logs
 *   <li>Manage transactional boundaries
 * </ul>
 */
public final class RemoveRolesService {

  private static final Logger LOGGER = LogManager.getLogger();

  private final RemoveRoleUseCase removeRoleUseCase;
  private final ApplicationAuditLogServiceFactory auditLogServiceFactory;
  private final Clock clock;

  @Inject
  RemoveRolesService(
      RemoveRoleUseCase removeRoleUseCase,
      ApplicationAuditLogServiceFactory auditLogServiceFactory,
      Clock clock) {
    this.removeRoleUseCase = removeRoleUseCase;
    this.auditLogServiceFactory = auditLogServiceFactory;
    this.clock = clock;
  }

  /**
   * Removes one or more roles and creates audit log entries.
   *
   * <p>This method orchestrates:
   *
   * <ol>
   *   <li>Role removal (userrepository-core) - for each identifier
   *   <li>Audit log creation (audit-core) - only for successfully removed roles
   *   <li>Batch parent log creation - if multiple roles are being removed
   * </ol>
   *
   * <p>Built-in roles (e.g., Administrator with ID "1") are skipped and logged as warnings.
   *
   * @param request the removal request containing identifiers and optional audit parent ID
   * @throws com.sitepark.ies.sharedkernel.security.AccessDeniedException if role removal is not
   *     allowed
   * @throws com.sitepark.ies.userrepository.core.domain.exception.RoleNotFoundException if a role
   *     does not exist
   * @throws com.sitepark.ies.sharedkernel.anchor.AnchorNotFoundException if an anchor does not
   *     exist
   */
  public void removeRoles(@NotNull RemoveRolesServiceRequest request) {

    if (request.isEmpty()) {
      return;
    }

    Instant timestamp = Instant.now(this.clock);

    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(timestamp, request.auditParentId());

    String parentId =
        request.identifiers().size() > 1
            ? auditLogService.createBatchLog(Role.class, AuditBatchLogAction.BATCH_REMOVE)
            : request.auditParentId();
    auditLogService.updateParentId(parentId);

    int removedCount = 0;
    int skippedCount = 0;

    for (Identifier identifier : request.identifiers()) {
      RemoveRoleResult result =
          this.removeRoleUseCase.removeRole(
              RemoveRoleRequest.builder().identifier(identifier).build());

      switch (result) {
        case RemoveRoleResult.Removed removed -> {
          auditLogService.createLog(
              EntityRef.of(Role.class, removed.roleId()),
              removed.roleName(),
              AuditLogAction.REMOVE,
              removed.snapshot(),
              null);
          removedCount++;
        }
        case RemoveRoleResult.Skipped skipped -> {
          if (LOGGER.isWarnEnabled()) {
            LOGGER.warn("Skipped removal of role '{}': {}", skipped.roleId(), skipped.reason());
          }
          skippedCount++;
        }
      }
    }

    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Removed {} role(s), skipped {}", removedCount, skippedCount);
    }
  }
}
