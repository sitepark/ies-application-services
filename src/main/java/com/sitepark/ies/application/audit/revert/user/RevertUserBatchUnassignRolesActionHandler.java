package com.sitepark.ies.application.audit.revert.user;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.audit.AuditBatchLogAction;
import com.sitepark.ies.application.audit.revert.RevertEntityActionHandler;
import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.application.user.AssignRolesToUsersService;
import com.sitepark.ies.application.user.AssignRolesToUsersServiceRequest;
import com.sitepark.ies.audit.core.service.AuditLogService;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import com.sitepark.ies.userrepository.core.usecase.user.AssignRolesToUsersRequest;
import jakarta.inject.Inject;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

public class RevertUserBatchUnassignRolesActionHandler implements RevertEntityActionHandler {

  private final ApplicationAuditLogServiceFactory auditLogServiceFactory;
  private final AuditLogService auditLogService;
  private final AssignRolesToUsersService assignRolesToUsersService;
  private final Clock clock;

  @Inject
  RevertUserBatchUnassignRolesActionHandler(
      ApplicationAuditLogServiceFactory auditLogServiceFactory,
      AuditLogService auditLogService,
      AssignRolesToUsersService assignRolesToUsersService,
      Clock clock) {
    this.auditLogServiceFactory = auditLogServiceFactory;
    this.auditLogService = auditLogService;
    this.assignRolesToUsersService = assignRolesToUsersService;
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
        this.createRevertBatchAssignRolesLog(timestamp, request.parentId());

    for (String childId : childIds) {
      List<String> roleIds;
      try {
        roleIds = this.auditLogService.getBackwardDataList(childId, String.class);
      } catch (IOException e) {
        throw new RevertFailedException(request, "Failed to deserialize roleIds", e);
      }

      this.assignRolesToUsersService.assignRolesToUsers(
          AssignRolesToUsersServiceRequest.builder()
              .assignRolesToUsersRequest(
                  AssignRolesToUsersRequest.builder()
                      .userIdentifiers(b -> b.id(request.target().id()))
                      .roleIdentifiers(b -> b.ids(roleIds))
                      .build())
              .auditParentId(auditLogService.parentId())
              .build());
    }
  }

  private ApplicationAuditLogService createRevertBatchAssignRolesLog(
      Instant timestamp, String auditParentId) {
    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(timestamp, auditParentId);
    String batchId =
        auditLogService.createBatchLog(User.class, AuditBatchLogAction.REVERT_BATCH_UNASSIGN_ROLES);
    auditLogService.updateParentId(batchId);
    return auditLogService;
  }
}
