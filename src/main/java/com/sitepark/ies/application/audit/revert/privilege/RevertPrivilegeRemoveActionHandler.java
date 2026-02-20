package com.sitepark.ies.application.audit.revert.privilege;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.application.audit.revert.RevertEntityActionHandler;
import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.audit.core.domain.value.AuditLogTarget;
import com.sitepark.ies.audit.core.service.AuditLogService;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.userrepository.core.domain.entity.Privilege;
import com.sitepark.ies.userrepository.core.domain.value.PrivilegeSnapshot;
import com.sitepark.ies.userrepository.core.usecase.privilege.RestorePrivilegeRequest;
import com.sitepark.ies.userrepository.core.usecase.privilege.RestorePrivilegeResult;
import com.sitepark.ies.userrepository.core.usecase.privilege.RestorePrivilegeUseCase;
import jakarta.inject.Inject;
import java.io.IOException;

public class RevertPrivilegeRemoveActionHandler implements RevertEntityActionHandler {

  private final ApplicationAuditLogServiceFactory auditLogServiceFactory;
  private final AuditLogService auditLogService;
  private final RestorePrivilegeUseCase restorePrivilegeUseCase;

  @Inject
  RevertPrivilegeRemoveActionHandler(
      ApplicationAuditLogServiceFactory auditLogServiceFactory,
      AuditLogService auditLogService,
      RestorePrivilegeUseCase restorePrivilegeUseCase) {
    this.auditLogServiceFactory = auditLogServiceFactory;
    this.auditLogService = auditLogService;
    this.restorePrivilegeUseCase = restorePrivilegeUseCase;
  }

  @Override
  public void revert(RevertRequest request) {
    try {
      PrivilegeSnapshot restoreData =
          this.auditLogService.deserialize(request.backwardData(), PrivilegeSnapshot.class);
      RestorePrivilegeResult result =
          this.restorePrivilegeUseCase.restorePrivilege(new RestorePrivilegeRequest(restoreData));
      if (result instanceof RestorePrivilegeResult.Restored restored) {
        this.createRestoreAuditLog(restored, request.parentId());
      }
    } catch (IOException e) {
      throw new RevertFailedException(request, "Failed to deserialize privilege-snapshot", e);
    }
  }

  private void createRestoreAuditLog(
      RestorePrivilegeResult.Restored restored, String auditParentId) {

    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(restored.timestamp(), auditParentId);

    auditLogService.createLog(
        AuditLogTarget.of(
            Privilege.class, restored.privilegeId(), restored.snapshot().privilege().name()),
        AuditLogAction.RESTORE,
        null,
        restored.snapshot());
  }
}
