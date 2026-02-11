package com.sitepark.ies.application.privilege;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.audit.AuditBatchLogAction;
import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.userrepository.core.domain.entity.Privilege;
import com.sitepark.ies.userrepository.core.usecase.privilege.CreatePrivilegeResult;
import com.sitepark.ies.userrepository.core.usecase.privilege.CreatePrivilegeUseCase;
import com.sitepark.ies.userrepository.core.usecase.role.AssignPrivilegesToRolesResult;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Application Service that orchestrates privilege creation operations with cross-cutting concerns.
 *
 * <p>This service coordinates privilege creation and associated cross-cutting concerns like audit
 * logging, allowing controllers to perform complete creation operations without managing these
 * concerns themselves.
 *
 * <p><b>Responsibilities:</b>
 *
 * <ul>
 *   <li>Create privilege via userrepository-core
 *   <li>Create audit log entries for privilege creation
 *   <li>Create audit log entries for role assignments
 * </ul>
 */
public final class CreatePrivilegeService {

  private final CreatePrivilegeUseCase createPrivilegeUseCase;
  private final ApplicationAuditLogServiceFactory auditLogServiceFactory;

  @Inject
  CreatePrivilegeService(
      CreatePrivilegeUseCase createPrivilegeUseCase,
      ApplicationAuditLogServiceFactory auditLogServiceFactory) {
    this.createPrivilegeUseCase = createPrivilegeUseCase;
    this.auditLogServiceFactory = auditLogServiceFactory;
  }

  /**
   * Creates a new privilege and creates audit log entries.
   *
   * <p>This method orchestrates:
   *
   * <ol>
   *   <li>Privilege creation (userrepository-core)
   *   <li>Audit log creation (audit-core) - for privilege creation
   *   <li>Audit log creation (audit-core) - for role assignments if present
   * </ol>
   *
   * @param request the creation request containing privilege data, role identifiers, and optional
   *     audit parent ID
   * @return the created privilege ID
   * @throws com.sitepark.ies.sharedkernel.security.AccessDeniedException if privilege creation is
   *     not allowed
   * @throws com.sitepark.ies.sharedkernel.anchor.AnchorAlreadyExistsException if anchor already
   *     exists
   */
  public String createPrivilege(@NotNull CreatePrivilegeServiceRequest request) {

    CreatePrivilegeResult result =
        this.createPrivilegeUseCase.createPrivilege(request.createPrivilegeRequest());

    this.createCreationAuditLog(result, request.auditParentId());

    if (result.roleAssignmentResult() != null
        && result.roleAssignmentResult()
            instanceof AssignPrivilegesToRolesResult.Assigned assigned) {
      this.createRoleAssignmentAuditLogs(assigned, result, request.auditParentId());
    }

    return result.privilegeId();
  }

  protected void createCreationAuditLog(CreatePrivilegeResult result, String auditParentId) {

    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(result.timestamp(), auditParentId);
    auditLogService.createLog(
        EntityRef.of(Privilege.class, result.privilegeId()),
        result.snapshot().privilege().name(),
        AuditLogAction.CREATE,
        null,
        result.snapshot());
  }

  protected void createRoleAssignmentAuditLogs(
      AssignPrivilegesToRolesResult.Assigned result,
      CreatePrivilegeResult createResult,
      @Nullable String auditParentId) {

    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(result.timestamp(), auditParentId);

    var assignments = result.assignments();

    String parentId =
        assignments.size() > 1
            ? auditLogService.createBatchLog(
                Privilege.class, AuditBatchLogAction.BATCH_ASSIGN_PRIVILEGES)
            : auditParentId;
    auditLogService.updateParentId(parentId);

    for (String roleId : assignments.roleIds()) {
      auditLogService.createLog(
          EntityRef.of(Privilege.class, createResult.privilegeId()),
          createResult.snapshot().privilege().name(),
          AuditLogAction.ASSIGN_PRIVILEGES,
          assignments.privilegeIds(roleId),
          assignments.privilegeIds(roleId));
    }
  }
}
