package com.sitepark.ies.application.audit.revert.role;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.audit.AuditBatchLogAction;
import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.application.audit.revert.RevertEntityActionHandler;
import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.audit.core.service.AuditLogService;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.userrepository.core.domain.entity.Role;
import com.sitepark.ies.userrepository.core.usecase.role.AssignPrivilegesToRolesRequest;
import com.sitepark.ies.userrepository.core.usecase.role.AssignPrivilegesToRolesUseCase;
import jakarta.inject.Inject;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

public class RevertRoleBatchUnassignPrivilegesActionHandler implements RevertEntityActionHandler {

  private final ApplicationAuditLogServiceFactory auditLogServiceFactory;
  private final AuditLogService auditLogService;
  private final AssignPrivilegesToRolesUseCase assignPrivilegesToRolesUseCase;
  private final Clock clock;

  @Inject
  RevertRoleBatchUnassignPrivilegesActionHandler(
      ApplicationAuditLogServiceFactory auditLogServiceFactory,
      AuditLogService auditLogService,
      AssignPrivilegesToRolesUseCase assignPrivilegesToRolesUseCase,
      Clock clock) {
    this.auditLogServiceFactory = auditLogServiceFactory;
    this.auditLogService = auditLogService;
    this.assignPrivilegesToRolesUseCase = assignPrivilegesToRolesUseCase;
    this.clock = clock;
  }

  @Override
  public void revert(RevertRequest request) {
    List<String> childIds = this.auditLogService.getRecursiveChildIds(request.id());
    if (childIds.isEmpty()) {
      return;
    }

    Instant timestamp = Instant.now(this.clock);
    ApplicationAuditLogService auditLogService =
        this.createRevertBatchUnassignRolesLog(timestamp, request.parentId());

    for (String childId : childIds) {
      List<String> privilegeIds;
      try {
        privilegeIds = this.auditLogService.getBackwardDataList(childId, String.class);
      } catch (IOException e) {
        throw new RevertFailedException(request, "Failed to deserialize privilegeIds", e);
      }

      this.assignPrivilegesToRolesUseCase.assignPrivilegesToRoles(
          AssignPrivilegesToRolesRequest.builder()
              .roleIdentifiers(b -> b.id(request.target().id()))
              .privilegeIdentifiers(b -> b.ids(privilegeIds))
              .build());
      auditLogService.createLog(
          request.target(), AuditLogAction.ASSIGN_PRIVILEGES, privilegeIds, privilegeIds);
    }
  }

  private ApplicationAuditLogService createRevertBatchUnassignRolesLog(
      Instant timestamp, String auditParentId) {
    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(timestamp, auditParentId);
    String batchId =
        auditLogService.createBatchLog(
            Role.class, AuditBatchLogAction.REVERT_BATCH_UNASSIGN_PRIVILEGES);
    auditLogService.updateParentId(batchId);
    return auditLogService;
  }
}
