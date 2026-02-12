package com.sitepark.ies.application.audit.revert.privilege;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.audit.AuditBatchLogAction;
import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.application.audit.revert.RevertEntityActionHandler;
import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.audit.core.service.AuditLogService;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.userrepository.core.domain.entity.Privilege;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import com.sitepark.ies.userrepository.core.domain.value.PrivilegeSnapshot;
import com.sitepark.ies.userrepository.core.usecase.privilege.RestorePrivilegeRequest;
import com.sitepark.ies.userrepository.core.usecase.privilege.RestorePrivilegeUseCase;
import com.sitepark.ies.userrepository.core.usecase.user.CreateUserResult;
import jakarta.inject.Inject;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class RevertPrivilegeBatchRemoveActionHandler implements RevertEntityActionHandler {

  private final ApplicationAuditLogServiceFactory auditLogServiceFactory;
  private final AuditLogService auditLogService;
  private final RestorePrivilegeUseCase restorePrivilegeUseCase;
  private final Clock clock;

  @Inject
  RevertPrivilegeBatchRemoveActionHandler(
      ApplicationAuditLogServiceFactory auditLogServiceFactory,
      AuditLogService auditLogService,
      RestorePrivilegeUseCase restorePrivilegeUseCase,
      Clock clock) {
    this.auditLogServiceFactory = auditLogServiceFactory;
    this.auditLogService = auditLogService;
    this.restorePrivilegeUseCase = restorePrivilegeUseCase;
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
      PrivilegeSnapshot restoreData;
      try {
        Optional<PrivilegeSnapshot> dataOpt =
            this.auditLogService.getBackwardData(childId, PrivilegeSnapshot.class);
        restoreData =
            dataOpt.orElseThrow(
                () -> new RevertFailedException(request, "No backward data for log " + childId));
      } catch (IOException e) {
        throw new RevertFailedException(request, "Failed to deserialize privilege-snapshot", e);
      }

      this.restorePrivilegeUseCase.restorePrivilege(new RestorePrivilegeRequest(restoreData));
      auditLogService.createLog(
          EntityRef.of(User.class, restoreData.privilege().id()),
          restoreData.privilege().name(),
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
        auditLogService.createBatchLog(Privilege.class, AuditBatchLogAction.REVERT_BATCH_REMOVE);
    auditLogService.updateParentId(batchId);
    return auditLogService;
  }

  protected void createCreationAuditLog(CreateUserResult result, String auditParentId) {

    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(result.timestamp(), auditParentId);
    auditLogService.createLog(
        EntityRef.of(User.class, result.userId()),
        result.snapshot().user().toDisplayName(),
        AuditLogAction.CREATE,
        null,
        result.snapshot());
  }
}
