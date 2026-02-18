package com.sitepark.ies.application.privilege;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.audit.AuditBatchLogAction;
import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.application.label.ReassignLabelsToEntitiesService;
import com.sitepark.ies.application.label.ReassignLabelsToEntitiesServiceRequest;
import com.sitepark.ies.label.core.usecase.ReassignLabelsToEntitiesRequest;
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
  private final ReassignLabelsToEntitiesService reassignLabelsToEntitiesService;
  private final ApplicationAuditLogServiceFactory auditLogServiceFactory;

  @Inject
  CreatePrivilegeService(
      CreatePrivilegeUseCase createPrivilegeUseCase,
      ReassignLabelsToEntitiesService reassignLabelsToEntitiesService,
      ApplicationAuditLogServiceFactory auditLogServiceFactory) {
    this.createPrivilegeUseCase = createPrivilegeUseCase;
    this.reassignLabelsToEntitiesService = reassignLabelsToEntitiesService;
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

    this.createAuditLogs(result, request.auditParentId());

    if (!request.labelIdentifiers().isEmpty()) {
      ReassignLabelsToEntitiesServiceRequest labelRequest =
          ReassignLabelsToEntitiesServiceRequest.builder()
              .reassignLabelsToEntitiesRequest(
                  ReassignLabelsToEntitiesRequest.builder()
                      .entityRefs(
                          configure ->
                              configure.set(EntityRef.of(Privilege.class, result.privilegeId())))
                      .labelIdentifiers(
                          configure -> configure.identifiers(request.labelIdentifiers()))
                      .build())
              .auditParentId(request.auditParentId())
              .build();
      this.reassignLabelsToEntitiesService.reassignEntitiesFromLabels(labelRequest);
    }

    return result.privilegeId();
  }

  protected void createAuditLogs(CreatePrivilegeResult result, String auditParentId) {
    this.createCreationAuditLog(result, auditParentId);
    this.createPrivilegeCreationAuditLog(result, auditParentId);
  }

  private void createCreationAuditLog(CreatePrivilegeResult result, String auditParentId) {

    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(result.timestamp(), auditParentId);
    auditLogService.createLog(
        EntityRef.of(Privilege.class, result.privilegeId()),
        result.snapshot().privilege().name(),
        AuditLogAction.CREATE,
        null,
        result.snapshot());
  }

  private void createPrivilegeCreationAuditLog(
      CreatePrivilegeResult result, @Nullable String auditParentId) {

    if (!(result.roleAssignmentResult()
        instanceof AssignPrivilegesToRolesResult.Assigned createResult)) {
      return;
    }

    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(result.timestamp(), auditParentId);

    var assignments = createResult.assignments();

    String parentId =
        assignments.size() > 1
            ? auditLogService.createBatchLog(
                Privilege.class, AuditBatchLogAction.BATCH_ASSIGN_PRIVILEGES)
            : auditParentId;
    auditLogService.updateParentId(parentId);

    for (String roleId : assignments.roleIds()) {
      auditLogService.createLog(
          EntityRef.of(Privilege.class, result.privilegeId()),
          result.snapshot().privilege().name(),
          AuditLogAction.ASSIGN_PRIVILEGES,
          assignments.privilegeIds(roleId),
          assignments.privilegeIds(roleId));
    }
  }
}
