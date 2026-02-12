package com.sitepark.ies.application.audit.revert.role;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.audit.AuditBatchLogAction;
import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.application.audit.revert.RevertEntityActionHandler;
import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.audit.core.service.AuditLogService;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.userrepository.core.domain.entity.Role;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import com.sitepark.ies.userrepository.core.domain.value.RoleSnapshot;
import com.sitepark.ies.userrepository.core.usecase.role.RestoreRoleRequest;
import com.sitepark.ies.userrepository.core.usecase.role.RestoreRoleUseCase;
import jakarta.inject.Inject;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class RevertRoleBatchRemoveActionHandler implements RevertEntityActionHandler {

  private final ApplicationAuditLogServiceFactory auditLogServiceFactory;
  private final AuditLogService auditLogService;
  private final RestoreRoleUseCase restoreRoleUseCase;
  private final Clock clock;

  @Inject
  RevertRoleBatchRemoveActionHandler(
      ApplicationAuditLogServiceFactory auditLogServiceFactory,
      AuditLogService auditLogService,
      RestoreRoleUseCase restoreRoleUseCase,
      Clock clock) {
    this.auditLogServiceFactory = auditLogServiceFactory;
    this.auditLogService = auditLogService;
    this.restoreRoleUseCase = restoreRoleUseCase;
    this.clock = clock;
  }

  @Override
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  public void revert(RevertRequest request) {
    List<String> childIds = this.auditLogService.getRecursiveChildIds(request.id());
    if (childIds.isEmpty()) {
      return;
    }

    Instant timestamp = Instant.now(this.clock);
    ApplicationAuditLogService auditLogService =
        this.createRevertBatchRemoveLog(timestamp, request.parentId());

    for (String childId : childIds) {
      RoleSnapshot restoreData;
      try {
        Optional<RoleSnapshot> dataOpt =
            this.auditLogService.getBackwardData(childId, RoleSnapshot.class);
        restoreData =
            dataOpt.orElseThrow(
                () -> new RevertFailedException(request, "No backward data for log " + childId));
      } catch (IOException e) {
        throw new RevertFailedException(request, "Failed to deserialize role-snapshot", e);
      }

      this.restoreRoleUseCase.restoreRole(new RestoreRoleRequest(restoreData));
      auditLogService.createLog(
          EntityRef.of(User.class, restoreData.role().id()),
          restoreData.role().name(),
          AuditLogAction.RESTORE,
          null,
          restoreData);
    }
  }

  private ApplicationAuditLogService createRevertBatchRemoveLog(
      Instant timestamp, String auditParentId) {
    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(timestamp, auditParentId);
    String batchId =
        auditLogService.createBatchLog(Role.class, AuditBatchLogAction.REVERT_BATCH_REMOVE);
    auditLogService.updateParentId(batchId);
    return auditLogService;
  }
}
