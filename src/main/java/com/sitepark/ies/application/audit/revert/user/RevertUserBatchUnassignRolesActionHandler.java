package com.sitepark.ies.application.audit.revert.user;

import com.sitepark.ies.application.audit.revert.RevertEntityActionHandler;
import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.audit.core.service.AuditLogService;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.userrepository.core.usecase.user.AssignRolesToUsersRequest;
import com.sitepark.ies.userrepository.core.usecase.user.AssignRolesToUsersUseCase;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.inject.Inject;
import java.io.IOException;
import java.time.Clock;
import java.util.List;

@SuppressFBWarnings("URF_UNREAD_FIELD")
public class RevertUserBatchUnassignRolesActionHandler implements RevertEntityActionHandler {

  private final AuditLogService auditLogService;

  private final AssignRolesToUsersUseCase assignRolesToUsersUseCase;

  private final Clock clock;

  @Inject
  RevertUserBatchUnassignRolesActionHandler(
      AuditLogService auditLogService,
      AssignRolesToUsersUseCase assignRolesToUsersUseCase,
      Clock clock) {
    this.auditLogService = auditLogService;
    this.assignRolesToUsersUseCase = assignRolesToUsersUseCase;
    this.clock = clock;
  }

  @Override
  public void revert(RevertRequest request) {
    List<String> childIds = this.auditLogService.getRecursiveChildIds(request.id());
    if (childIds.isEmpty()) {
      return;
    }

    // Instant now = Instant.now(this.clock);
    // String auditLogParentId = this.createRevertBatchUnassignRolesLog(now);

    for (String childId : childIds) {
      List<String> roleIds;
      try {
        roleIds = this.auditLogService.getBackwardDataList(childId, String.class);
      } catch (IOException e) {
        throw new RevertFailedException(request, "Failed to deserialize roleIds", e);
      }

      this.assignRolesToUsersUseCase.assignRolesToUsers(
          AssignRolesToUsersRequest.builder()
              .userIdentifiers(b -> b.id(request.target().id()))
              .roleIdentifiers(b -> b.ids(roleIds))
              .build());
    }
  }

  /*
  private String createRevertBatchUnassignRolesLog(Instant now) {
    return this.auditLogService.createAuditLog(
        new CreateAuditLogRequest(
            AuditLogEntityType.USER.name(),
            null,
            null,
            AuditLogAction.REVERT_BATCH_UNASSIGN_ROLES.name(),
            null,
            null,
            now,
            null));
  }
  */
}
