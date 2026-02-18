package com.sitepark.ies.application.role;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.audit.AuditBatchLogAction;
import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.application.label.ReassignLabelsToEntitiesService;
import com.sitepark.ies.application.label.ReassignLabelsToEntitiesServiceRequest;
import com.sitepark.ies.label.core.usecase.ReassignLabelsToEntitiesRequest;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.userrepository.core.domain.entity.Role;
import com.sitepark.ies.userrepository.core.usecase.role.AssignPrivilegesToRolesResult;
import com.sitepark.ies.userrepository.core.usecase.role.CreateRoleResult;
import com.sitepark.ies.userrepository.core.usecase.role.CreateRoleUseCase;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CreateRoleService {

  private final CreateRoleUseCase createRoleUseCase;
  private final ReassignLabelsToEntitiesService reassignLabelsToEntitiesService;
  private final ApplicationAuditLogServiceFactory auditLogServiceFactory;

  @Inject
  CreateRoleService(
      CreateRoleUseCase createRoleUseCase,
      ReassignLabelsToEntitiesService reassignLabelsToEntitiesService,
      ApplicationAuditLogServiceFactory auditLogServiceFactory) {
    this.createRoleUseCase = createRoleUseCase;
    this.reassignLabelsToEntitiesService = reassignLabelsToEntitiesService;
    this.auditLogServiceFactory = auditLogServiceFactory;
  }

  public String createRole(@NotNull CreateRoleServiceRequest request) {

    CreateRoleResult result = this.createRoleUseCase.createRole(request.createRoleRequest());

    this.createAuditLogs(result, request.auditParentId());

    if (!request.labelIdentifiers().isEmpty()) {
      ReassignLabelsToEntitiesServiceRequest labelRequest =
          ReassignLabelsToEntitiesServiceRequest.builder()
              .reassignLabelsToEntitiesRequest(
                  ReassignLabelsToEntitiesRequest.builder()
                      .entityRefs(
                          configure -> configure.set(EntityRef.of(Role.class, result.roleId())))
                      .labelIdentifiers(
                          configure -> configure.identifiers(request.labelIdentifiers()))
                      .build())
              .auditParentId(request.auditParentId())
              .build();
      this.reassignLabelsToEntitiesService.reassignEntitiesFromLabels(labelRequest);
    }

    return result.roleId();
  }

  protected void createAuditLogs(CreateRoleResult result, String auditParentId) {
    this.createCreationAuditLog(result, auditParentId);
    this.createPrivilegeAssignmentAuditLogs(result, auditParentId);
  }

  private void createCreationAuditLog(CreateRoleResult result, String auditParentId) {

    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(result.timestamp(), auditParentId);
    auditLogService.createLog(
        EntityRef.of(Role.class, result.roleId()),
        result.snapshot().role().name(),
        AuditLogAction.CREATE,
        null,
        result.snapshot());
  }

  private void createPrivilegeAssignmentAuditLogs(
      CreateRoleResult result, @Nullable String auditParentId) {

    if (!(result.privilegeAssignmentResult()
        instanceof AssignPrivilegesToRolesResult.Assigned assigned)) {
      return;
    }

    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(result.timestamp(), auditParentId);

    var assignments = assigned.assignments();

    String parentId =
        assignments.size() > 1
            ? auditLogService.createBatchLog(
                Role.class, AuditBatchLogAction.BATCH_ASSIGN_PRIVILEGES)
            : auditParentId;
    auditLogService.updateParentId(parentId);

    for (String roleId : assignments.roleIds()) {
      auditLogService.createLog(
          EntityRef.of(Role.class, result.roleId()),
          result.snapshot().role().name(),
          AuditLogAction.ASSIGN_PRIVILEGES,
          assignments.privilegeIds(roleId),
          assignments.privilegeIds(roleId));
    }
  }
}
