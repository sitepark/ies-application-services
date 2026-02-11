package com.sitepark.ies.application.audit.revert.privilege;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.audit.AuditBatchLogAction;
import com.sitepark.ies.application.audit.revert.RevertEntityActionHandler;
import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.audit.core.service.AuditLogService;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.userrepository.core.domain.entity.Privilege;
import com.sitepark.ies.userrepository.core.domain.value.PrivilegeSnapshot;
import com.sitepark.ies.userrepository.core.usecase.privilege.RestorePrivilegeRequest;
import com.sitepark.ies.userrepository.core.usecase.privilege.RestorePrivilegeUseCase;
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

    Instant now = Instant.now(this.clock);
    String auditLogParentId = this.createRevertBatchRemoveLog(now, request.parentId());

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

      this.restorePrivilegeUseCase.restorePrivilege(
          new RestorePrivilegeRequest(restoreData, auditLogParentId));
    }
  }

  private String createRevertBatchRemoveLog(Instant timestamp, String auditParentId) {
    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(timestamp, auditParentId);
    return auditLogService.createBatchLog(Privilege.class, AuditBatchLogAction.REVERT_BATCH_REMOVE);
  }
}
