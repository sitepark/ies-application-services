package com.sitepark.ies.application.audit.revert.role;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.audit.AuditBatchLogAction;
import com.sitepark.ies.application.audit.revert.RevertEntityActionHandler;
import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.audit.core.service.AuditLogService;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.userrepository.core.domain.entity.Role;
import com.sitepark.ies.userrepository.core.usecase.role.UnassignPrivilegesFromRolesRequest;
import com.sitepark.ies.userrepository.core.usecase.role.UnassignPrivilegesFromRolesUseCase;
import jakarta.inject.Inject;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

public class RevertRoleBatchAssignPrivilegesActionHandler implements RevertEntityActionHandler {

  private final ApplicationAuditLogServiceFactory auditLogServiceFactory;
  private final AuditLogService auditLogService;
  private final UnassignPrivilegesFromRolesUseCase unassignPrivilegesFromRolesUseCase;
  private final Clock clock;

  @Inject
  RevertRoleBatchAssignPrivilegesActionHandler(
      ApplicationAuditLogServiceFactory auditLogServiceFactory,
      AuditLogService auditLogService,
      UnassignPrivilegesFromRolesUseCase unassignPrivilegesFromRolesUseCase,
      Clock clock) {
    this.auditLogServiceFactory = auditLogServiceFactory;
    this.auditLogService = auditLogService;
    this.unassignPrivilegesFromRolesUseCase = unassignPrivilegesFromRolesUseCase;
    this.clock = clock;
  }

  @Override
  public void revert(RevertRequest request) {
    List<String> childIds = this.auditLogService.getRecursiveChildIds(request.id());
    if (childIds.isEmpty()) {
      return;
    }

    Instant timestamp = Instant.now(this.clock);
    String auditLogParentId = this.createRevertBatchAssignRolesLog(timestamp, request.parentId());

    for (String childId : childIds) {
      List<String> privilegesIds;
      try {
        privilegesIds = this.auditLogService.getBackwardDataList(childId, String.class);
      } catch (IOException e) {
        throw new RevertFailedException(request, "Failed to deserialize privilegeIds", e);
      }

      this.unassignPrivilegesFromRolesUseCase.unassignPrivilegesFromRoles(
          UnassignPrivilegesFromRolesRequest.builder()
              .roleIdentifiers(b -> b.id(request.target().id()))
              .privilegeIdentifiers(b -> b.ids(privilegesIds))
              .auditParentId(auditLogParentId)
              .build());
    }
  }

  private String createRevertBatchAssignRolesLog(Instant timestamp, String auditParentId) {
    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(timestamp, auditParentId);
    return auditLogService.createBatchLog(
        Role.class, AuditBatchLogAction.REVERT_BATCH_ASSIGN_PRIVILEGES);
  }
}
