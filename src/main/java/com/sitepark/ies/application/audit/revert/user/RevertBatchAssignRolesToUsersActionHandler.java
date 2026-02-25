package com.sitepark.ies.application.audit.revert.user;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.audit.AuditBatchLogAction;
import com.sitepark.ies.application.audit.revert.RevertEntityActionHandler;
import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.application.user.UnassignRolesFromUsersService;
import com.sitepark.ies.application.user.UnassignRolesFromUsersServiceRequest;
import com.sitepark.ies.audit.core.domain.entity.AuditLog;
import com.sitepark.ies.audit.core.service.AuditLogService;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import com.sitepark.ies.userrepository.core.usecase.user.UnassignRolesFromUsersRequest;
import jakarta.inject.Inject;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class RevertBatchAssignRolesToUsersActionHandler implements RevertEntityActionHandler {

  private final ApplicationAuditLogServiceFactory auditLogServiceFactory;
  private final AuditLogService auditLogService;
  private final UnassignRolesFromUsersService unassignRolesFromUsersService;
  private final Clock clock;

  @Inject
  RevertBatchAssignRolesToUsersActionHandler(
      ApplicationAuditLogServiceFactory auditLogServiceFactory,
      AuditLogService auditLogService,
      UnassignRolesFromUsersService unassignRolesFromUsersService,
      Clock clock) {
    this.auditLogServiceFactory = auditLogServiceFactory;
    this.auditLogService = auditLogService;
    this.unassignRolesFromUsersService = unassignRolesFromUsersService;
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
      AuditLog auditLog;
      List<String> roleIds;
      try {

        Optional<AuditLog> auditLogOpt = this.auditLogService.getAuditLog(childId);
        if (auditLogOpt.isEmpty()) {
          continue;
        }
        auditLog = auditLogOpt.get();
        roleIds = this.auditLogService.deserializeList(auditLog.backwardData(), String.class);
      } catch (IOException e) {
        throw new RevertFailedException(request, "Failed to deserialize roleIds", e);
      }

      this.unassignRolesFromUsersService.unassignRolesFromUsers(
          UnassignRolesFromUsersServiceRequest.builder()
              .unassignRolesFromUsersRequest(
                  UnassignRolesFromUsersRequest.builder()
                      .userIdentifiers(b -> b.id(auditLog.entityId()))
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
        auditLogService.createBatchLog(
            User.class, AuditBatchLogAction.BATCH_UNASSIGN_ROLES_FROM_USERS);
    auditLogService.updateParentId(batchId);
    return auditLogService;
  }
}
